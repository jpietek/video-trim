package com.cloud.video.editor.model.probe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
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

}
