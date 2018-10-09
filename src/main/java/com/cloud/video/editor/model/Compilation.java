package com.cloud.video.editor.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Compilation {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@OneToMany(cascade = {CascadeType.ALL}, mappedBy = "compilation")
	@JsonManagedReference
	private Set<Video> videos;
	
	@ManyToOne
	@JoinColumn
	@JsonBackReference
	private User user;
	
	private String name;
	private Double duration;
	private Date modified;
	private Integer fps;
	
	private Integer startFrame;
	
	public Integer getCompilationId() {
		return id;
	}
	public void setCompilationId(Integer compilationId) {
		this.id = compilationId;
	}
	
	public Double getDuration() {
		return duration;
	}
	public void setDuration(Double duration) {
		this.duration = duration;
	}
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getFps() {
		return fps;
	}
	public void setFps(Integer fps) {
		this.fps = fps;
	}
	public Set<Video> getVideos() {
		return videos;
	}
	public void setVideos(Set<Video> videos) {
		this.videos = videos;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getStartFrame() {
		return startFrame;
	}
	public void setStartFrame(Integer startFrame) {
		this.startFrame = startFrame;
	}
	
}
