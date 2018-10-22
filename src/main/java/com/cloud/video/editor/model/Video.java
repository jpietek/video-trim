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

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
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
	
	private long audioBitrate;
	private long videoBitrate;
	
	private String audioCodecName;
	private String videoCodecNAme;

	private String profile;
	private String level;
	private String pixFormat;

	@ManyToOne
	@JoinColumn
	@JsonBackReference
	private Compilation compilation;
	
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
}
