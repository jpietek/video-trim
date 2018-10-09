package com.cloud.video.editor.model.melt;

public class AudioTrack implements Comparable<AudioTrack>  {

	private String audioUrl;
	private int in;
	private int out;
	
	public AudioTrack() {
		
	}
	
	public AudioTrack(String audioUrl, int in, int out) {
		this.audioUrl = audioUrl;
		this.in = in;
		this.out = out;
	}
	
	public String getAudioUrl() {
		return audioUrl;
	}
	
	public String getAudioLocalPath() {
		return audioUrl;
	}
	public void setAudioUrl(String audioUrl) {
		this.audioUrl = audioUrl;
	}
	public int getIn() {
		return in;
	}
	public void setIn(int in) {
		this.in = in;
	}
	public int getOut() {
		return out;
	}
	public void setOut(int out) {
		this.out = out;
	}

	@Override
	public int compareTo(AudioTrack at) {
		return this.in - at.in;
	}

}
