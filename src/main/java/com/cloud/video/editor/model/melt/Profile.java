package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public class Profile {

	private int width;
	private int height;
	private int aspectRatioNumerator;
	private int aspectRatioDenominator;
	private int fpsNumerator;
	private int fpsDenominator;

	public Profile(int width, int height, int aspectRatioNum, int aspectRatioDen, int fpsNumerator, int fpsDenominator) {
		super();
		this.width = width;
		this.height = height;
		this.aspectRatioNumerator = aspectRatioNum;
		this.aspectRatioDenominator = aspectRatioDen;
		this.fpsNumerator = fpsNumerator;
		this.fpsDenominator = fpsDenominator;
	}
	public int getAspectRatioNum() {
		return aspectRatioNumerator;
	}
	public void setAspectRatioNum(int aspectRatioNum) {
		this.aspectRatioNumerator = aspectRatioNum;
	}
	public int getAspectRatioDen() {
		return aspectRatioDenominator;
	}
	public void setAspectRatioDen(int aspectRatioDen) {
		this.aspectRatioDenominator = aspectRatioDen;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	
	public int getFpsNumerator() {
		return fpsNumerator;
	}
	public void setFpsNumerator(int fpsNumerator) {
		this.fpsNumerator = fpsNumerator;
	}
	public int getFpsDenominator() {
		return fpsDenominator;
	}
	public void setFpsDenominator(int fpsDenominator) {
		this.fpsDenominator = fpsDenominator;
	}
	public Directives toXml() {		
		Directives profile = new Directives()
				.add("profile")
				.attr("width", this.width)
				.attr("height", this.height)
				.attr("progressive", 1)
				.attr("sample_aspect_num", 1)
				.attr("sample_aspect_den", 1)
				.attr("display_aspect_num", this.aspectRatioNumerator)
				.attr("display_aspect_den", this.aspectRatioDenominator)
				.attr("frame_rate_num", this.fpsNumerator)
				.attr("frame_rate_den", this.fpsDenominator);
				
		return profile;
	}
	
	public String toString() {
		return "description=custom" + System.lineSeparator()
			+ "frame_rate_num=" + this.fpsNumerator + System.lineSeparator()
			+ "frame_rate_den=" + this.fpsDenominator + System.lineSeparator()
			+ "width=" + this.width + System.lineSeparator()
			+ "height=" + this.height + System.lineSeparator()
			+ "progressive=1" + System.lineSeparator()
			+ "sample_aspect_num=1" + System.lineSeparator()
			+ "sample_aspect_den=1" + System.lineSeparator()
			+ "display_aspect_num=" + this.aspectRatioNumerator + System.lineSeparator()
			+ "display_aspect_den=" + this.aspectRatioDenominator + System.lineSeparator();
	}
	
}
