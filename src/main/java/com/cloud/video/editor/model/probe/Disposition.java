package com.cloud.video.editor.model.probe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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
		public Long get_default() {
			return _default;
		}
		public void set_default(Long _default) {
			this._default = _default;
		}
		public Long getDub() {
			return dub;
		}
		public void setDub(Long dub) {
			this.dub = dub;
		}
		public Long getOriginal() {
			return original;
		}
		public void setOriginal(Long original) {
			this.original = original;
		}
		public Long getComment() {
			return comment;
		}
		public void setComment(Long comment) {
			this.comment = comment;
		}
		public Long getLyrics() {
			return lyrics;
		}
		public void setLyrics(Long lyrics) {
			this.lyrics = lyrics;
		}
		public Long getKaraoke() {
			return karaoke;
		}
		public void setKaraoke(Long karaoke) {
			this.karaoke = karaoke;
		}
		public Long getForced() {
			return forced;
		}
		public void setForced(Long forced) {
			this.forced = forced;
		}
		public Long getHearing_impaired() {
			return hearing_impaired;
		}
		public void setHearing_impaired(Long hearing_impaired) {
			this.hearing_impaired = hearing_impaired;
		}
		public Long getVisual_impaired() {
			return visual_impaired;
		}
		public void setVisual_impaired(Long visual_impaired) {
			this.visual_impaired = visual_impaired;
		}
		public Long getClean_effects() {
			return clean_effects;
		}
		public void setClean_effects(Long clean_effects) {
			this.clean_effects = clean_effects;
		}
		public Long getAttached_pic() {
			return attached_pic;
		}
		public void setAttached_pic(Long attached_pic) {
			this.attached_pic = attached_pic;
		}
	
}
