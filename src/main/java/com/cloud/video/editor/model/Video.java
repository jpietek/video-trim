package com.cloud.video.editor.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.cloud.video.editor.model.melt.AspectRatioTransform;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Video {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	private String videoId;

	private String name;
	private String extension;
	private String path;
	
	private String thumbnailLink;
	
	@Column(length=512)
	private String directContentLink;
	
	@Column(length=512)
	private String webContentLink;
	private int width;
	private int height;
	private long duration;
	private Date created;
	private Date modified;
	private Date timeTaken;
	private long size;
	
	private double gpsLat;
	private double gpsLong;
	
	private double fps;
	private long frameCount;
	
	private double cutIn;
	private double cutOut;
	
	private int sortId;

	private double speed = 1.0;
	
	private String audioSource;
	
	@ManyToOne
	@JoinColumn
	@JsonBackReference
	private Compilation compilation;
	
	@OneToMany(cascade = {CascadeType.ALL}, mappedBy = "video")
	@JsonManagedReference
	private Set<AspectRatioTransform> affineTransforms;
	
	public Video() {
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getThumbnailLink() {
		return thumbnailLink;
	}
	public void setThumbnailLink(String thumbnailLink) {
		this.thumbnailLink = thumbnailLink;
	}
	public int getWidtn() {
		return width;
	}
	public void setWidtn(int widtn) {
		this.width = widtn;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}

	public String getDirectContentLink() {
		return directContentLink;
	}

	public void setDirectContentLink(String directContentLink) {
		this.directContentLink = directContentLink;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public Date getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(Date timeTaken) {
		this.timeTaken = timeTaken;
	}

	public double getGpsLat() {
		return gpsLat;
	}

	public void setGpsLat(double gpsLat) {
		this.gpsLat = gpsLat;
	}

	public double getGpsLong() {
		return gpsLong;
	}

	public void setGpsLong(double gpsLong) {
		this.gpsLong = gpsLong;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public double getFps() {
		return fps;
	}

	public void setFps(double fps) {
		this.fps = fps;
	}

	public long getFrameCount() {
		return frameCount;
	}

	public void setFrameCount(long frameCount) {
		this.frameCount = frameCount;
	}
	
	public double getCutIn() {
		return cutIn;
	}

	public void setCutIn(double cutIn) {
		this.cutIn = cutIn;
	}

	public double getCutOut() {
		return cutOut;
	}

	public double cutInSeconds() {
		return this.cutIn * this.duration / 1000;
	}

	public long cutInMillis() {
		return (long) Math.ceil(this.cutIn * this.duration);
	}
	
	public long cutOutMillis() {
		return (long) Math.ceil(this.cutOut * this.duration);
	}

	public double cutOutSeconds() {
		return this.cutOut * this.duration / 1000;
	}

	public void setCutOut(double cutOut) {
		this.cutOut = cutOut;
	}

	public int getSortId() {
		return sortId;
	}

	public void setSortId(int sortId) {
		this.sortId = sortId;
	}

	public String getWebContentLink() {
		return webContentLink;
	}

	public void setWebContentLink(String webContentLink) {
		this.webContentLink = webContentLink;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public String getAudioSource() {
		return audioSource;
	}

	public void setAudioSource(String audioSource) {
		this.audioSource = audioSource;
	}

	public Compilation getCompilation() {
		return compilation;
	}

	public void setCompilation(Compilation compilation) {
		this.compilation = compilation;
	}

	public Set<AspectRatioTransform> getAffineTransforms() {
		return affineTransforms;
	}

	public void setAffineTransforms(Set<AspectRatioTransform> affineTransforms) {
		this.affineTransforms = affineTransforms;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	
}
