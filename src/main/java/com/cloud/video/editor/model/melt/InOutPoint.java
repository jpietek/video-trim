package com.cloud.video.editor.model.melt;

public class InOutPoint {

	private int in;
	private int out;

	public InOutPoint() {

	}
	
	public InOutPoint (int in, int out) {
		this.in = in;
		this.out = out;
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

