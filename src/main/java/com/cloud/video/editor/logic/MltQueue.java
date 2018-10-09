package com.cloud.video.editor.logic;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.cloud.video.editor.model.Result;
import com.cloud.video.editor.model.VideoQuality;
import com.cloud.video.editor.model.melt.LivePreviewParams;
import com.cloud.video.editor.model.melt.MltCallException;
import com.cloud.video.editor.model.melt.MltRequest;
import com.cloud.video.editor.model.melt.MltUnit;
import com.cloud.video.editor.utils.NetUtils;
import com.cloud.video.editor.utils.SystemTools;

public class MltQueue {

	private static final int DEFAULT_FPS = 25;
	private static final VideoQuality DEFAULT_PREVIEW_QUALITY = new VideoQuality("hi");

	private PriorityBlockingQueue<MltUnit> availableMltUnits = new PriorityBlockingQueue<MltUnit>();

	private final Map<Long, MltUnit> activeUsers = Collections
			.synchronizedMap(new HashMap<Long, MltUnit>());

	private final Map<Long, String> wsUserAndToken = Collections
			.synchronizedMap(new HashMap<Long, String>());

	private int poolSize = 16;

	private final static Logger LOGGER = Logger.getLogger(MltQueue.class.getName());

	private final static int MLT_LOCK_TIMEOUT_SECONDS = 10;

	public int size() {
		return this.availableMltUnits.size();
	}

	public int mapsize() {
		return this.activeUsers.size();
	}

	public MltQueue(int poolSize) {
		this.poolSize = poolSize;
	}

	public void setup() {
		this.startNewMltUnits(poolSize);
	}

	public void clear() {
		this.availableMltUnits.clear();
	}

	public void addUserToken(long wsUserId, String token) {
		this.wsUserAndToken.put(wsUserId, token);
	}

	public boolean checkUserToken(long wsUserId, String checkToken) {
		String token = wsUserAndToken.get(wsUserId);
		if (token == null) {
			return false;
		}

		return token.equals(checkToken) ? true : false;
	}

	private Result startAndSetupMltProcess(MltUnit mu) {
		LOGGER.info("mlt start: " + mu.getMltPort());
		boolean res = SystemTools.executeCommand(
				"/apps/melt/start-melted-server " + mu.getMltPort());
		if (res == false) {
			return new Result(false, "failed to start new mlt process", null, 7);
		}

		final int maxPolls = 200;
		int i = 0;
		while (!NetUtils.serverListening("localhost", mu.getMltPort())) {
			if (i > maxPolls) {
				return new Result(false,
						"failed to start new mlt process, " + "timed out after 10s", 13);
			}
			try {
				i++;
				Thread.sleep(50);
			} catch (InterruptedException e) {
				return new Result(false, "interrupted", 12);
			}
		}

		Result setupRes = this.setupMltUnit(mu);

		if (!setupRes.isSuccess()) {
			return setupRes;
		}

		return new Result(true, "mlt process started");
	}

	public void startNewMltUnits(int capacity) {
		int currentSize = activeUsers.size() + availableMltUnits.size();
		LOGGER.info("current size: " + currentSize);

		for (int i = currentSize; i < currentSize + capacity; i++) {
			LOGGER.info("adding mlt index: " + i);
			MltUnit newUnit = new MltUnit(null, i);
			if (this.startAndSetupMltProcess(newUnit).isSuccess()) {
				LOGGER.info("added new unit: " + availableMltUnits.size());
				availableMltUnits.add(newUnit);
			}
		}
	}

	public Result updateMltUnit(long wsUserId, MltUnit mu) {

		MltUnit prevMu = activeUsers.put(wsUserId, mu);

		if (prevMu == null) {
			new Result(false, "mlt unit already expired");
		}

		return new Result(true, "mlt unit updated");
	}

	public Result reserveMltUnit(long userWebsocketId, Integer previewId,
			LivePreviewParams lpp) {

		LOGGER.info("reserve mlt unit start: " + userWebsocketId + " " + this.size() + " "
				+ this.mapsize());
		MltUnit mu = activeUsers.get(userWebsocketId);
		if (mu == null) {
			try {
				mu = availableMltUnits.poll(20, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				return new Result(false, "reserve mlt unit interrupted");
			}
			activeUsers.put(userWebsocketId, mu);
		}

		if (mu == null) {
			return new Result(false,
					"max mlt pool size reached, and nothing freed-up in 20s, sorry", null, 5);
		}

		stopWsNotifier(mu);
		mu.initExpire();
		mu.setPreviewId(previewId);

		final MltRequest req = new MltRequest(mu);

		try {
			mu.getLock().tryLock(MLT_LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			return new Result(false, "interrupted");
		}

		LOGGER.info("setup mlt unit");
		try {
			if (lpp.getCustomProfile() != null) {
				req.cmd("stop u0");
				req.cmd("uset u0 consumer.mlt_profile=" + lpp.getCustomProfile());
				req.cmd("uset u0 consumer.g=" + lpp.getFps());
				req.cmd("uset u0 consumer.r=" + lpp.getFps());
				req.cmd("uset u0 consumer.keyint_min=" + lpp.getFps());
				req.cmd("uset u0 consumer.vb=" + lpp.getBitrate());
			}

			req.cmd("load u0 " + previewId + "/" + lpp.getXmlFile());
			req.cmd("play u0 1000");

		} catch (MltCallException e) {
			LOGGER.info("mlt play exception");
			e.printStackTrace();
			return new Result(false,
					"exception while calling play to mlt unit: " + e.getMessage(), null, 10);
		}

		req.close();
		
		mu.getLock().unlock();

		LOGGER.info("reserve mlt unit stop: " + userWebsocketId + " " + this.size() + " "
				+ this.mapsize());

		return new Result(true, "mlt unit allocated ok", mu);
	}

	public Result getMltUnit(long websocketUserId) {
		MltUnit mu = activeUsers.get(websocketUserId);
		if (mu == null) {
			return new Result(false,
					"preview stopped being active, generate a new one", null, 1);
		}

		return new Result(true, "seek ok", mu);
	}

	public Result stopMltUnit(Long websocketUserId) {
		LOGGER.info("stop mlt unit start: " + websocketUserId + " " + this.size() + " "
				+ this.mapsize());
		MltUnit mu = activeUsers.get(websocketUserId);

		if (mu == null) {
			return new Result(false, "mlt unit not found", null, 2);
		}

		try {
			stopWsNotifier(mu);
			activeUsers.remove(websocketUserId);
			wsUserAndToken.remove(websocketUserId);
			LOGGER.info("mlt stop: " + mu.getMltPort());
			MltRequest stopReq = new MltRequest(mu);
			stopReq.cmd("stop u0");
			stopReq.close();

			mu.setPreviewId(null);
			availableMltUnits.offer(mu);
		} catch (Exception e) {
			LOGGER.info("exception while stopping ws notofier: " + mu.getMltPort() + " "
					+ e.getMessage());
		}

		LOGGER.info("stop mlt unit stop: " + this.size() + " " + this.mapsize());
		return new Result(true, "mlt unit added back to the available pool");
	}

	private void stopWsNotifier(MltUnit mu) {
		if (mu != null && mu.getWsNotifier() != null) {
			LOGGER.info("stop ws notif: " + mu.getMltPort());
			mu.getWsNotifier().interrupt();
			mu.setWsNotifier(null);
		}
	}

	public Result restartMltUnit(MltUnit mu) {

		LOGGER.info("mlt unit restart: " + mu.getMltPort());
		MltRequest stopReq = new MltRequest(mu);
		try {
			stopReq.cmd("stop u0");
		} catch (MltCallException e) {
			e.printStackTrace();
		}
		stopReq.close();

		boolean res = SystemTools.executeCommand(
				"/apps/melt/kill-single-melted " + mu.getMltPort());
		if (res == false) {
			return new Result(false, "failed to kill mlt process", null, 7);
		}

		this.startAndSetupMltProcess(mu);

		mu.setPreviewsDone(0);

		return new Result(true, "mlt unit added back to the available pool");
	}

	private Result setupMltUnit(MltUnit mu) {
		LOGGER.info("mlt setup: " + mu.getMltPort());
		MltRequest req = new MltRequest(mu);
		try {
			LOGGER.info("cons index: " + mu.getMltInstanceId());
			FileUtils.forceMkdir(new File(mu.getBasepath()));
			req.cmd("uadd avformat:" + mu.getPlaylistPath());
			int mltUnitIndex = 0;
			req.cmd("uset u" + mltUnitIndex + " consumer.hls_time=1");
			req.cmd("uset u" + mltUnitIndex + " consumer.vcodec=libx264");
			req.cmd("uset u" + mltUnitIndex + " consumer.preset=veryfast");
			req.cmd("uset u" + mltUnitIndex + " consumer.real_time=-1");
			req.cmd("uset u" + mltUnitIndex + " consumer.threads=8");
			req.cmd("uset u" + mltUnitIndex + " consumer.progressive=1");
			req.cmd("uset u" + mltUnitIndex + " consumer.segment_time=1");
			req.cmd("uset u" + mltUnitIndex + " consumer.strict=experimental");
			req.cmd("uset u" + mltUnitIndex + " consumer.hls_list_size=3600");
			req.close();
		} catch (MltCallException e) {
			e.printStackTrace();
			req.close();
			LOGGER.info("melted call failed during unit setup");
			return new Result(false,
					"melted call failed during unit setup," + "id: " + mu.getMltInstanceId(),
					4);
		} catch (IOException e) {
			e.printStackTrace();
			req.close();
			LOGGER.info("can't create dst dir for melted consumer");
			return new Result(false,
					"can't create dst dir for melted consumer: " + mu.getMltInstanceId(), 6);
		}

		LOGGER.info("melt setup ok");
		return new Result(true, "mlt unit setup ok");
	}

	public int getDefaultFps() {
		return DEFAULT_FPS;
	}

	public VideoQuality getDefaultPreviewQuality() {
		return DEFAULT_PREVIEW_QUALITY;
	}

	public PriorityBlockingQueue<MltUnit> getAvailableMltUnits() {
		return availableMltUnits;
	}

	public void setAvailableMltUnits(PriorityBlockingQueue<MltUnit> availableMltUnits) {
		this.availableMltUnits = availableMltUnits;
	}

	public Map<Long, MltUnit> getActiveUsers() {
		return activeUsers;
	}

	public Map<Long, String> getWsUserAndToken() {
		return wsUserAndToken;
	}

	public int getPoolSize() {
		return poolSize;
	}

}
