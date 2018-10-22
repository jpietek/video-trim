package com.cloud.video.editor.model.probe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Disposition {

	private Long _default;
	private Long dub;
	private Long original;
	private Long comment;
	private Long lyrics;
	private Long karaoke;
	private Long forced;
	private Long hearing_impaired;
	private Long visual_impaired;
	private Long clean_effects;
	private Long attached_pic;

}
