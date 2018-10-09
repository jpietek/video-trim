package com.cloud.video.editor.model.melt;

import java.util.HashMap;
import java.util.Map;

import org.xembly.Directives;

public class WebVfxProducer {

	private String id;
	private int length;
	private String htmlTemplate;
	private HashMap<String, String> templateProperties;
	
	public WebVfxProducer(int id, String htmlTemplate, int length, HashMap<String, String> templateProperties) {
		this.id = "producer" + id;
		this.htmlTemplate = htmlTemplate;
		this.templateProperties = templateProperties;
		this.length = length;
	}
	
	public Directives toXml() {
		Directives producer = new Directives()
				.xpath("/mlt")
				.add("producer")
				.attr("id", this.id)
				.attr("out", this.length)
				.add("property")
				.attr("name", "length")
				.set(this.length)
				.up()
				.add("property")
				.attr("name", "mlt_type")
				.set("webvfx")
				.up()
				.add("property")
				.attr("name", "transparent")
				.set("1")
				.up()
				.add("property")
				.attr("name", "resource")
				.set("/var/www/html/templates");

		for (Map.Entry<String, String> prop : templateProperties.entrySet()) {
			producer.append(new Directives()
				.up()
				.add("property")
				.attr("name", prop.getKey())
				.set(prop.getValue()));
		}
		return producer;
	}
}
