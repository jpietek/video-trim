package com.cloud.video.editor.model.melt;

public class Watermark {

	private String watermarkUrl;
	
	private int xOffsetPrecentage;
	private int yOffsetPrecentage;
	private int xScalePercentage;
	private int yScalePercentage;
	
	private int in;
	private int out;

	public Watermark() {
		
	}
	
	public Watermark(String watermarkUrl, int in, int out, int xOffsetPrecentage, 
			int yOffsetPrecentage, int xScalePercentage, int yScalePercentage) {
		this.watermarkUrl = watermarkUrl;
		this.in = in;
		this.out = out;
		this.xOffsetPrecentage = xOffsetPrecentage;
		this.yOffsetPrecentage = yOffsetPrecentage;
		this.xScalePercentage = xScalePercentage;
		this.yScalePercentage = yScalePercentage;
	}
	
	public String getWatermarkUrl() {
		return watermarkUrl;
	}

	public void setWatermarkUrl(String watermarkUrl) {
		this.watermarkUrl = watermarkUrl;
	}

	public int getxOffsetPrecentage() {
		return xOffsetPrecentage;
	}

	public void setxOffsetPrecentage(int xOffsetPrecentage) {
		this.xOffsetPrecentage = xOffsetPrecentage;
	}

	public int getyOffsetPrecentage() {
		return yOffsetPrecentage;
	}

	public void setyOffsetPrecentage(int yOffsetPrecentage) {
		this.yOffsetPrecentage = yOffsetPrecentage;
	}

	public int getxScalePercentage() {
		return xScalePercentage;
	}

	public void setxScalePercentage(int xScalePercentage) {
		this.xScalePercentage = xScalePercentage;
	}

	public int getyScalePercentage() {
		return yScalePercentage;
	}

	public void setyScalePercentage(int yScalePercentage) {
		this.yScalePercentage = yScalePercentage;
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
