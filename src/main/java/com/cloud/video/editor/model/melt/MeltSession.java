package com.cloud.video.editor.model.melt;

import java.util.ArrayList;
import java.util.List;

import com.cloud.video.editor.model.Video;
import com.cloud.video.editor.model.VideoQuality;

public class MeltSession {

	private List<Transition> transitions = new ArrayList<Transition>();

	private List<OverlayClip> overlayClips = new ArrayList<OverlayClip>();

	private List<Watermark> watermarks = new ArrayList<Watermark>();

	private List<AudioTrack> audioTracks = new ArrayList<AudioTrack>();

	private int aspectRatioNumerator = 16;
	private int aspectRatioDenominator = 9;
	
	private int fpsNumerator;
	private int fpsDenominator = 1;
	
	private VideoQuality sessionQuality;

	private int volumeRatio = 0;
	
	private int startFrame = 0;

	public MeltSession() {

	}

	public List<Transition> getTransitions() {
		return transitions;
	}
	
	public boolean containsLumaTransitionPerClip(Video c) {
		for(Transition t: this.transitions) {
			if(t.getTransitionType() == TransitionType.LUMA && t.getClip1Id() == c.getSortId()) {
				return true;
			}
		}
		return false;
	}

	public void setTransitions(List<Transition> transitions) {
		this.transitions = transitions;
	}

	public void setOverlayClips(List<OverlayClip> overlayClips) {
		this.overlayClips = overlayClips;
	}

	public List<Watermark> getWatermarks() {
		return watermarks;
	}

	public void setWatermarks(List<Watermark> watermarks) {
		this.watermarks = watermarks;
	}

	public List<AudioTrack> getAudioTracks() {
		return audioTracks;
	}

	public void setAudioTracks(List<AudioTrack> audioTracks) {
		this.audioTracks = audioTracks;
	}

	public List<OverlayClip> getOverlayClips() {
		return overlayClips;
	}

	public int getVolumeRatio() {
		return volumeRatio;
	}

	public void setVolumeRatio(int volumeRatio) {
		this.volumeRatio = volumeRatio;
	}

	public int getAspectRatioNumerator() {
		return aspectRatioNumerator;
	}

	public void setAspectRatioNumerator(int aspectRatioNumerator) {
		this.aspectRatioNumerator = aspectRatioNumerator;
	}

	public int getAspectRatioDenominator() {
		return aspectRatioDenominator;
	}

	public void setAspectRatioDenominator(int aspectRatioDenominator) {
		this.aspectRatioDenominator = aspectRatioDenominator;
	}

	public int getFpsNumerator() {
		return fpsNumerator;
	}

	public void setFpsNumerator(int fpsNumerator) {
		this.fpsNumerator = fpsNumerator;
	}

	public int getFpsDenominator() {
		return fpsDenominator;
	}

	public void setFpsDenominator(int fpsDenominator) {
		this.fpsDenominator = fpsDenominator;
	}

	public VideoQuality getSessionQuality() {
		return sessionQuality;
	}

	public void setSessionQuality(VideoQuality sessionQuality) {
		this.sessionQuality = sessionQuality;
	}

	public int getStartFrame() {
		return startFrame;
	}

	public void setStartFrame(int startFrame) {
		this.startFrame = startFrame;
	}
	
}
