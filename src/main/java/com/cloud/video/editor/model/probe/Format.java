package com.cloud.video.editor.model.probe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Format { 
	 
    private Long nb_streams; 
    private Long nb_programs;
    private Long probe_score;
    private String format_name; 
    private String format_long_name; 
    private String start_time; 
    private String duration; 
    private String size; 
    private String bit_rate;
	public Long getNb_streams() {
		return nb_streams;
	}
	public void setNb_streams(Long nb_streams) {
		this.nb_streams = nb_streams;
	}
	public Long getNb_programs() {
		return nb_programs;
	}
	public void setNb_programs(Long nb_programs) {
		this.nb_programs = nb_programs;
	}
	public Long getProbe_score() {
		return probe_score;
	}
	public void setProbe_score(Long probe_score) {
		this.probe_score = probe_score;
	}
	public String getFormat_name() {
		return format_name;
	}
	public void setFormat_name(String format_name) {
		this.format_name = format_name;
	}
	public String getFormat_long_name() {
		return format_long_name;
	}
	public void setFormat_long_name(String format_long_name) {
		this.format_long_name = format_long_name;
	}
	public String getStart_time() {
		return start_time;
	}
	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getBit_rate() {
		return bit_rate;
	}
	public void setBit_rate(String bit_rate) {
		this.bit_rate = bit_rate;
	}
}
