package com.cloud.video.editor.logic;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import com.cloud.video.editor.model.Compilation;
import com.cloud.video.editor.model.CompilationRepository;
import com.cloud.video.editor.model.Result;
import com.cloud.video.editor.model.melt.LivePreviewParams;
import com.cloud.video.editor.model.melt.MltRequest;
import com.cloud.video.editor.model.melt.MltUnit;
import com.cloud.video.editor.model.melt.PreviewStats;
import com.cloud.video.editor.utils.SystemTools;

public class LivePreviewLogic {

	final int statsPollingIntervalMillis = 1000;
	private WatchService watcher;
	private Map<WatchKey, String> watchPaths = new ConcurrentHashMap<WatchKey, String>();
	private MltQueue mltQueue;

	private final static int WATCH_KEY_TIMEOUT_SECONDS = 3;

	private final static int WATCH_POLLING_INTERVAL = 300;

	private final static int MAX_WATCH_RETRIES = 100;

	private final static int LIVEPREVIEW_PLAYLIST_SLEEP = 3000;

	private final static int MAX_STALLED_NOTIFICATIONS = 10;

	private final static Logger LOGGER = Logger
			.getLogger(LivePreviewLogic.class.getName());
	
	@Autowired
	private CompilationRepository compilationRepository;
	
	@Autowired
	private PreviewLogic previewLogic;

	private Result updateMltUnitExpire(long wsUserId, MltUnit mu) {
		mu.incExpire();
		return mltQueue.updateMltUnit(wsUserId, mu);
	}

	private boolean sleepTrue() {
		try {
			Thread.sleep(LIVEPREVIEW_PLAYLIST_SLEEP);
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	private boolean watchModified(MltUnit mu, int segmentsToBuffer) {
		long pastModified = 0;
		int modifiedCount = 0;
		int retries = 0;

		while (true) {
			File playlistFile = new File(mu.getPlaylistPath());

			if (retries > MAX_WATCH_RETRIES) {
				LOGGER.info("max watch retries reached, return false");
				return false;
			}

			if (!playlistFile.exists()) {
				try {
					Thread.sleep(WATCH_POLLING_INTERVAL);
				} catch (InterruptedException e) {
				}
				retries++;
				continue;
			}

			long curModified = playlistFile.lastModified();

			if (curModified > pastModified) {
				LOGGER.info("watch playlist, inc modified count: " + modifiedCount);
				modifiedCount++;
			}

			pastModified = curModified;

			if (modifiedCount == segmentsToBuffer) {
				LOGGER.info("watch playlist req segments to buffer " + segmentsToBuffer);
				return true;
			}

			try {
				Thread.sleep(WATCH_POLLING_INTERVAL);
			} catch (InterruptedException e) {
				break;
			}

			retries++;
		}
		return false;
	}

	@SuppressWarnings("unused")
	@Deprecated
	// Live preview playlist watch function based on inotify. Doesn't seem to
	// work well with nfs 4.1 mounts
	private boolean watchPlaylist(MltUnit mu, int segmentsToBuffer) {
		try {
			WatchKey key = Paths.get(mu.getBasepath()).register(watcher,
					StandardWatchEventKinds.ENTRY_MODIFY,
					StandardWatchEventKinds.ENTRY_CREATE);
			this.watchPaths.put(key, mu.getBasepath());
		} catch (IOException e) {
			LOGGER.info("exception while waiting for playlist, returning anyway");
			sleepTrue();
		}

		int i = 0;
		for (;;) {
			WatchKey key = null;
			try {
				key = watcher.poll(WATCH_KEY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				LOGGER.warning(
						"exception while polling for playlist: " + mu.getPlaylistUrl());
				break;
			}

			if (key == null) {
				LOGGER.info(WATCH_KEY_TIMEOUT_SECONDS
						+ "s time while watching the playlist key, assume the playlist is there and return true");
				return true;
			}

			String watchBasepath = this.watchPaths.get(key);
			if (watchBasepath == null) {
				LOGGER.info(
						"Found a key, not matching the ones previously registered, continue");
				continue;
			}

			if (!watchBasepath.equalsIgnoreCase(mu.getBasepath())) {
				LOGGER.info(
						"Found some other key, not matching the current mlt unit basepath, try again");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();
				if (kind == StandardWatchEventKinds.OVERFLOW) {
					LOGGER.info("watch key overflow");
					continue;
				}

				final Path changed = (Path) event.context();
				if (changed.toString().endsWith(".m3u8")) {
					i++;
					if (i >= segmentsToBuffer) {
						LOGGER.info("playlist found: " + i + " " + changed.toString()
								+ ", map size: " + this.watchPaths.size());
						this.watchPaths.remove(key);
						key.cancel();
						return true;
					}
				}
			}
			if (!key.reset()) {
				break;
			}
		}
		return true;
	}

	public void init() {
		mltQueue = new MltQueue(16);
		mltQueue.setup();
		try {
			LOGGER.info("init watcher");
			watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			LOGGER.info("can't init playlist watch inotify service");
		}
	}

	public void kill() {
		SystemTools.executeCommand("/apps/melt/stop-melted-server");
		mltQueue.clear();
	}

	public Result play(final String xmlBasePath, final Integer previewId,
			final long webSocketUserId, final String authToken,
			final LivePreviewParams lpp) {

		Result res = mltQueue.reserveMltUnit(webSocketUserId, previewId, lpp);

		if (!res.isSuccess()) {
			return res;
		}

		MltUnit mu = (MltUnit) res.getResult();

		boolean playlistResult = watchModified(mu, lpp.getSegmentsToBuffer());

		if (!playlistResult) {
			mltQueue.restartMltUnit(mu);
			return new Result(false,
					"preview not generated within 30s timeout, mlt unit restarted", null, 8);
		}

		if (authToken != null) {
			mltQueue.addUserToken(webSocketUserId, authToken);
		}

		Thread wsNotifier = new Thread(() -> {
			try {
				int prevFrame = 0;
				int curFrame = 0;
				int stalledCount = 0;
				while (!Thread.interrupted()) {
					final MltRequest statReq = new MltRequest(mu);
					Result statsResult = statReq.stats();
					statReq.close();

					if (Calendar.getInstance().getTime().after(mu.getExpire())) {
						LOGGER.info("mlt session expired: " + webSocketUserId + " "
								+ mu.getMltInstanceId());

						stop(webSocketUserId);
						return;
					}

					if (statsResult.isSuccess()) {
						PreviewStats s = (PreviewStats) statsResult.getResult();
						curFrame = Integer.parseInt(s.curFrame);
						if (curFrame == prevFrame) {
							stalledCount++;
						}

						prevFrame = curFrame;

						if (webSocketUserId > 0 && stalledCount < MAX_STALLED_NOTIFICATIONS) {
							s.duration = String.valueOf(lpp.getDuration());
							LOGGER.info("mlt unit send stat " + mu.getMltPort()
									+ this.mltQueue.size() + " " + this.mltQueue.mapsize() + " "
									+ statsResult.getResult() + " " + statsResult.isSuccess() + " "
									+ mu.getExpire() + " " + Calendar.getInstance().getTime());
							// JmsSender.sendNotificationMessage(previewId, s.toJson(),
							// Arrays.asList(String.valueOf(webSocketUserId)));
						}

					} else {
						stop(webSocketUserId);
						return;
					}

					try {
						Thread.sleep(statsPollingIntervalMillis);
					} catch (InterruptedException e) {
						break;
					}
				}
			} catch (Exception e) {
				LOGGER.info("exception in live preview notification loop " + e.getMessage());
				return;
			}
		});
		wsNotifier.start();
		mu.setWsNotifier(wsNotifier);
		mu.incPreviewsDone();
		return new Result(true, "live preview started ok", mu.getPlaylistUrl());

	}

	public Result seek(long websocketUserId, int startFrame, boolean flushPlaylist) {
		Result res = mltQueue.getMltUnit(websocketUserId);
		if (!res.isSuccess()) {
			return res;
		}

		MltUnit mu = (MltUnit) res.getResult();
		Result updateResult = this.updateMltUnitExpire(websocketUserId, mu);

		if (!updateResult.isSuccess()) {
			return updateResult;
		}

		Compilation c = compilationRepository.findById(mu.getPreviewId());
		c.setStartFrame(startFrame);
		mu.incPreviewsDone();
		return previewLogic.playMultiPreview(c, websocketUserId, null, false);
	}

	public Result stop(long websocketUserId) {
		Result res = mltQueue.stopMltUnit(websocketUserId);
		return res;
	}

	public boolean checkUserToken(long wsUserId, String checkToken) {
		return mltQueue.checkUserToken(wsUserId, checkToken);
	}

	public Result recoverMltUnit(int mltInstanceId) {
		MltUnit newUnit = new MltUnit(null, mltInstanceId);
		if (this.mltQueue.getAvailableMltUnits().contains(newUnit)) {
			return new Result(false, "unit already in the mlt queue");
		} else {
			this.mltQueue.getAvailableMltUnits().offer(newUnit);
			return new Result(false, "added new mlt unit, id: " + mltInstanceId);
		}

	}

	public Result heartbeat(long websocketUserId) {

		Result getMuResult = mltQueue.getMltUnit(websocketUserId);

		if (!getMuResult.isSuccess()) {
			return getMuResult;
		}

		MltUnit mu = (MltUnit) getMuResult.getResult();
		Result updateResult = this.updateMltUnitExpire(websocketUserId, mu);

		if (!updateResult.isSuccess()) {
			return updateResult;
		}
		return new Result(true, "expired updated to now + 2min", null, 0);
	}

	public MltQueue getMltQueue() {
		return mltQueue;
	}

	public void setMltQueue(MltQueue mltQueue) {
		this.mltQueue = mltQueue;
	}

	public WatchService getWatcher() {
		return watcher;
	}

	public void setWatcher(WatchService watcher) {
		this.watcher = watcher;
	}

	public Map<WatchKey, String> getWatchPaths() {
		return watchPaths;
	}

	public void setWatchPaths(Map<WatchKey, String> watchPaths) {
		this.watchPaths = watchPaths;
	}

}