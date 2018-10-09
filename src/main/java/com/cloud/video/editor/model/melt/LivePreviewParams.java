package com.cloud.video.editor.model.melt;

public class LivePreviewParams {

	private final String customProfile;
	private long bitrate;
	private int fps;
	private int duration;
	private String xmlFile;
	private int segmentsToBuffer;
	
	public LivePreviewParams(String customProfile, long bitrate, int fps, 
			int duration, String xmlFile, int segmentsToBuffer) {
		
		this.customProfile = customProfile;
		this.bitrate = bitrate;
		this.fps = fps;
		this.duration = duration;
		this.xmlFile = xmlFile;
		this.segmentsToBuffer = segmentsToBuffer;
	}


	public long getBitrate() {
		return bitrate;
	}


	public void setBitrate(long bitrate) {
		this.bitrate = bitrate;
	}


	public int getFps() {
		return fps;
	}


	public void setFps(int fps) {
		this.fps = fps;
	}

	public int getDuration() {
		return duration;
	}


	public void setDuration(int duration) {
		this.duration = duration;
	}


	public String getCustomProfile() {
		return customProfile;
	}


	public String getXmlFile() {
		return xmlFile;
	}


	public void setXmlFile(String xmlFile) {
		this.xmlFile = xmlFile;
	}


	public int getSegmentsToBuffer() {
		return segmentsToBuffer;
	}


	public void setSegmentsToBuffer(int segmentsToBuffer) {
		this.segmentsToBuffer = segmentsToBuffer;
	}
	
}
