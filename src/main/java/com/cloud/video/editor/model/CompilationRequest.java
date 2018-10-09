package com.cloud.video.editor.model;

import java.util.Set;

public class CompilationRequest {

	private Set<Video> videos;
	private String userMail;
	private Double totalDuration;
	private String name;
	

	public Set<Video> getVideos() {
		return videos;
	}
	public void setVideos(Set<Video> videos) {
		this.videos = videos;
	}
	public String getUserMail() {
		return userMail;
	}
	public void setUserMail(String userMail) {
		this.userMail = userMail;
	}
	public Double getTotalDuration() {
		return totalDuration;
	}
	public void setTotalDuration(Double totalDuration) {
		this.totalDuration = totalDuration;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
