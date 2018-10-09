package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public class ColorProducer extends Producer {
	private String color;
	
	public ColorProducer(String color) {
		this.color = color;
	}
	
	public String getId() {
		return this.color;
	}
	
	public Directives toXml() {
		Directives producer = new Directives()
				.xpath("/mlt/producers")
				.add("producer")
				.attr("id", this.color)
				.add("property")
				.attr("name", "mlt_type")
				.set("producer")
				.up()
				.add("property")
				.attr("name", "mlt_service")
				.set("colour")
				.up()
				.add("property")
				.attr("name", "resource")
				.set(this.color);
		return producer;
	}
}
