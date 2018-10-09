package com.cloud.video.editor.model.melt;

public class MeltTransition {

	private String id;
	private Integer length;

	public MeltTransition() {

	}

	public MeltTransition(String id) {
		this.id = id;
	}

	public MeltTransition(String id, int length) {
		this.id = id;
		this.length = length;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
}
