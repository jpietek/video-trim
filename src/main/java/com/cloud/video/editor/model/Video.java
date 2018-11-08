package com.cloud.video.editor.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class Video {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@Column(length = 512)
	private String directContentLink;

	@Column(length = 512)
	private String webContentLink;

	@ManyToOne
	@JoinColumn
	@JsonBackReference
	private Compilation compilation;

	private String videoId;
	private String name;
	private String extension;
	private String path;
	private String thumbnailLink;
	private int width;
	private int height;
	protected long duration;
	private Date created;
	private Date modified;
	private Date timeTaken;
	private long size;
	private double gpsLat;
	private double gpsLong;
	private double fps;
	private long frameCount;
	protected double cutIn;
	protected double cutOut;
	private int sortId;
	private double speed = 1.0;
	private String audioSource;
	private long audioBitrate;
	private long videoBitrate;
	private String audioCodecName;
	private String videoCodecNAme;
	private String profile;
	private String level;
	private String pixFormat;

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

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDirectContentLink() {
		return directContentLink;
	}

	public void setDirectContentLink(String directContentLink) {
		this.directContentLink = directContentLink;
	}

	public String getWebContentLink() {
		return webContentLink;
	}

	public void setWebContentLink(String webContentLink) {
		this.webContentLink = webContentLink;
	}

	public Compilation getCompilation() {
		return compilation;
	}

	public void setCompilation(Compilation compilation) {
		this.compilation = compilation;
	}

	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getThumbnailLink() {
		return thumbnailLink;
	}

	public void setThumbnailLink(String thumbnailLink) {
		this.thumbnailLink = thumbnailLink;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
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

	public Date getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(Date timeTaken) {
		this.timeTaken = timeTaken;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
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

	public void setCutOut(double cutOut) {
		this.cutOut = cutOut;
	}

	public int getSortId() {
		return sortId;
	}

	public void setSortId(int sortId) {
		this.sortId = sortId;
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

	public long getAudioBitrate() {
		return audioBitrate;
	}

	public void setAudioBitrate(long audioBitrate) {
		this.audioBitrate = audioBitrate;
	}

	public long getVideoBitrate() {
		return videoBitrate;
	}

	public void setVideoBitrate(long videoBitrate) {
		this.videoBitrate = videoBitrate;
	}

	public String getAudioCodecName() {
		return audioCodecName;
	}

	public void setAudioCodecName(String audioCodecName) {
		this.audioCodecName = audioCodecName;
	}

	public String getVideoCodecNAme() {
		return videoCodecNAme;
	}

	public void setVideoCodecNAme(String videoCodecNAme) {
		this.videoCodecNAme = videoCodecNAme;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getPixFormat() {
		return pixFormat;
	}

	public void setPixFormat(String pixFormat) {
		this.pixFormat = pixFormat;
	}

}
