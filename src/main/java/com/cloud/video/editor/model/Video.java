package com.cloud.video.editor.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.cloud.video.editor.utils.HttpUtils;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

@Entity
@Data
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
    private String basepath;

    private final static String TS_SUFFIX = ".ts";


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

    public String getFullSegmentPath(ChunkType chunk) {
        return HttpUtils.buildURL(basepath, this.id + "-" + chunk.toString().toLowerCase() + TS_SUFFIX);
    }

    public String getTrimmedSegmentPath(ChunkType chunk) {
        return HttpUtils.buildURL(basepath, this.id + "-"
                + chunk.toString().toLowerCase() + "-trimmed" + TS_SUFFIX);

    }

}
