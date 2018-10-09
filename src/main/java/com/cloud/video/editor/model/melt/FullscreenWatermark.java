package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public class FullscreenWatermark extends Filter implements FilterInterface {
	
	private String watermarkPath;
	
	public FullscreenWatermark() {
		
	}
	
	public FullscreenWatermark(int id, int in, int out, String watermarkPath) {
		this.setId(id);
		this.setIn(in);
		this.setOut(out);
		this.watermarkPath = watermarkPath;
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
				.set("watermark");
		
			return filter;
	}

	public String getWatermarkPath() {
		return watermarkPath;
	}

	public void setWatermarkPath(String watermarkPath) {
		this.watermarkPath = watermarkPath;
	}

}
