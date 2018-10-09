package com.cloud.video.editor.model.melt;

public class Filter {
	
	private String id;	
	private int in;
	private int out;
	
	public String getId() {
		return id;
	}
	public void setId(int id) {
		this.id = "filter" + id;
	}
	
	public int getIn() {
		return in;
	}
	public void setIn(int in) {
		this.in = in;
	}
	public int getOut() {
		return out;
	}
	public void setOut(int out) {
		this.out = out;
	}
}
