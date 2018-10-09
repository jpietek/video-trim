package com.cloud.video.editor.model.melt;

import java.io.Serializable;

public class Transition {

	private static final long serialVersionUID = 11293801823L;
	
	private int clip1Id;
	private int clip2Id;
	
	private TransitionType transitionType;
	
	private int transitionDuration;
	
	private String mediaPath;
	
	public Transition() {
		
	}
	
	public Transition(int clip1Id, int clip2Id, TransitionType transitionType, int transitionDuration) {
		this.clip1Id = clip1Id;
		this.clip2Id = clip2Id;
		this.transitionType = transitionType;
		this.transitionDuration = transitionDuration;
	}
	
	public TransitionType getTransitionType() {
		return transitionType;
	}
	public void setTransitionType(TransitionType transitionType) {
		this.transitionType = transitionType;
	}
	public int getTransitionDuration() {
		return transitionDuration;
	}
	public void setTransitionDuration(int transitionDuration) {
		this.transitionDuration = transitionDuration;
	}
	
	public int getClip1Id() {
		return clip1Id;
	}
	public void setClip1Id(int clip1Id) {
		this.clip1Id = clip1Id;
	}
	public int getClip2Id() {
		return clip2Id;
	}
	public void setClip2Id(int clip2Id) {
		this.clip2Id = clip2Id;
	}

	public String getMediaPath() {
		return mediaPath;
	}

	public void setMediaPath(String mediaPath) {
		this.mediaPath = mediaPath;
	}
}
