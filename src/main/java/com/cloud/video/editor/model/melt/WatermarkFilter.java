package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public class WatermarkFilter extends Filter implements FilterInterface {
	private String watermarkPath;
	int xOffsetPrecentage;
	int yOffsetPrecentage;
	int xScalePercentage;
	int yScalePercentage;
	
	public WatermarkFilter() {
		
	}
	
	public WatermarkFilter(int id, int in, int out, String watermarkPath, 
			int xOffsetPrecentage, int yOffsetPrecentage, int xScalePercentage, int yScalePercentage) {
		this.setId(id);
		this.setIn(in);
		this.setOut(out);
		this.watermarkPath = watermarkPath;
		this.xOffsetPrecentage = xOffsetPrecentage;
		this.yOffsetPrecentage = yOffsetPrecentage;
		this.xScalePercentage = xScalePercentage;
		this.yScalePercentage = yScalePercentage;		
	}
	
	public Directives toXml() {
		Directives filter = new Directives()
				.add("filter")
				.attr("id", this.getId())
				.attr("in", this.getIn())
				.attr("out", this.getOut())
				.add("property")
				.attr("name", "factory")
				.set("loader")
				.up()
				.add("property")
				.attr("name", "resource")
				.set(watermarkPath)
				.up()
				.add("property")
				.attr("name", "mlt_service")
				.set("watermark")
				.up()
				.add("property")
				.attr("name", "composite.geometry")
				.set(xOffsetPrecentage + "%," + yOffsetPrecentage + "%," + xScalePercentage + "%,"
						+ yScalePercentage +"%");
		
			return filter;
	}

	public String getWatermarkPath() {
		return watermarkPath;
	}

	public void setWatermarkPath(String watermarkPath) {
		this.watermarkPath = watermarkPath;
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
}
