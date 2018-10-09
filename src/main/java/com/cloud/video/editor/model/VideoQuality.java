package com.cloud.video.editor.model;

public class VideoQuality implements Comparable<VideoQuality> {

	public String name;
	public Integer width;
	public Integer height;
	public Long bandwidth;

	public VideoQuality(String name) {
		VideoQuality q = PossibleQualities.qualities.get(name);
		this.name = q.name;
		this.width = q.width;
		this.height = q.height;
		this.bandwidth = q.bandwidth;
	}

	public VideoQuality(String name, Integer width, Integer height, Long bandwidth) {
		super();
		this.name = name;
		this.width = width;
		this.height = height;
		this.bandwidth = bandwidth;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VideoQuality other = (VideoQuality) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	public Integer getHeight() {
		return height;
	}
	public void setHeight(Integer height) {
		this.height = height;
	}
	public Long getBandwidth() {
		return bandwidth;
	}
	public void setBandwidth(Long bandwidth) {
		this.bandwidth = bandwidth;
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
