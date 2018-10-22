package com.cloud.video.editor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class VideoQuality implements Comparable<VideoQuality> {

	private String name;
	private Integer width;
	private Integer height;
	private Long bandwidth;

	public VideoQuality(String name) {
		VideoQuality q = PossibleQualities.qualities.get(name);
		this.name = q.name;
		this.width = q.width;
		this.height = q.height;
		this.bandwidth = q.bandwidth;
	}

	public String getPlaylistQualityString() {
		return "#EXT-X-STREAM-INF:PROGRAM-ID=1,RESOLUTION=" + this.getWidth() + "x" + this.getHeight()
		+ ",BANDWIDTH=" + this.getBandwidth();
	}
	
	public String toString() {
		return this.name + " " + this.width + " " + this.height + " " + this.bandwidth;
	}

	@Override
	public int compareTo(VideoQuality o) {
		return (int) (this.bandwidth - o.getBandwidth());
	}
}
