package com.cloud.video.editor.model.melt;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.cloud.video.editor.utils.HttpUtils;

public class MltUnit implements Comparable<MltUnit> {
	private Integer previewId;
	private int mltInstanceId;
	private Date expire;
	private Thread wsNotifier;
	private int previewsDone = 0;
	private ReentrantLock lock = new ReentrantLock();

	public void initExpire() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 2);
		this.expire = cal.getTime();
	}

	public MltUnit(Integer previewId, int mltInstanceId) {
		this.mltInstanceId = mltInstanceId;
		this.previewId = previewId;
		this.initExpire();
	}

	public void incExpire() {
		Calendar cal = new GregorianCalendar();
		cal.setTime(expire);
		cal.add(Calendar.MINUTE, 2);
		this.expire = cal.getTime();
	}

	public int getMltInstanceId() {
		return mltInstanceId;
	}

	public void setMltInstanceId(int mltInstanceId) {
		this.mltInstanceId = mltInstanceId;
	}

	public Integer getPreviewId() {
		return previewId;
	}

	public void setPreviewId(Integer previewId) {
		this.previewId = previewId;
	}

	public int getMltPort() {
		return 10000 + this.mltInstanceId;
	}

	@Override
	public int compareTo(MltUnit mu) {
		return this.mltInstanceId - mu.mltInstanceId;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(mltInstanceId).toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		boolean result;
		if ((other == null) || (getClass() != other.getClass())) {
			result = false;
		} else {
			MltUnit otherUnit = (MltUnit) other;
			result = (mltInstanceId == otherUnit.getMltInstanceId());
		}

		return result;
	}

	public String getPlaylistPath() {
		return "/data/live_previews/p" + this.mltInstanceId
				+ "/master.m3u8";
	}

	public String getPlaylistUrl() {
		return HttpUtils.buildURL("http://test/",
				"live_previews/p" + Integer.toString(this.mltInstanceId),
				"master.m3u8");
	}

	public String getBasepath() {
		return "/data/live_previews/p" + this.mltInstanceId;
	}

	public Date getExpire() {
		return expire;
	}

	public void setExpire(Date expire) {
		this.expire = expire;
	}


	public Thread getWsNotifier() {
		return wsNotifier;
	}

	public void setWsNotifier(Thread wsNotifier) {
		this.wsNotifier = wsNotifier;
	}

	public int getPreviewsDone() {
		return previewsDone;
	}
	
	public void incPreviewsDone() {
		this.previewsDone++;
	}

	public void setPreviewsDone(int previewsDone) {
		this.previewsDone = previewsDone;
	}

	public ReentrantLock getLock() {
		return lock;
	}

	public void setLock(ReentrantLock lock) {
		this.lock = lock;
	}
	
}