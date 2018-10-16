package com.cloud.video.editor.model.probe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Stream { 
	 
    private Long index; 
    private String codec_name; 
    private String codec_long_name; 
    private String profile; 
    private String codec_type; 
    private String codec_time_base; 
    private String codec_tag_string; 
    private String codec_tag; 
    private Long width; 
    private Long height; 
    private Long has_b_frames; 
    private String sample_aspect_ratio; 
    private String display_aspect_ratio; 
    private String pix_fmt; 
    private Long level; 
    private String r_frame_rate; 
    private String avg_frame_rate; 
    private String time_base; 
    private Long start_pts; 
    private String start_time; 
    private Long bit_rate; 
    private Disposition disposition; 
    private Tags tags;
    private String sample_fmt; 
    private String sample_rate; 
    private Long channels; 
    private String channel_layout; 
    private Long bits_per_sample; 
    private Long duration_ts; 
    private String duration;
    private Long nb_frames;
}

