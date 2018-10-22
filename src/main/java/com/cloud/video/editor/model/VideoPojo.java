package com.cloud.video.editor.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class VideoPojo {
	private Integer id;
	private String videoId;
	private String name;
	private String extension;
	private String path;
	private String thumbnailLink;
	private String directContentLink;
	private String webContentLink;
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
	private Compilation compilation;
}
