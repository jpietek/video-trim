package com.cloud.video.editor.model.melt;

import java.util.HashMap;
import java.util.Map;

import org.xembly.Directives;

public class WebVfxFilter extends Filter implements FilterInterface {

	private String htmlTemplate;
	private HashMap<String, String> templateProperties;
	private String quality;
	private int fps;

	public WebVfxFilter() {

	}

	public WebVfxFilter(int id, int in, int out, String htmlTemplate, 
			HashMap<String, String> templateProperties, String quality, int fps) {
		this.setId(id);
		this.setIn(in);
		this.setOut(out);
		this.htmlTemplate = htmlTemplate;
		this.templateProperties = templateProperties;
		this.quality = quality;
		this.fps = fps;
	}

	public Directives toXml() {
		Directives filter = new Directives()
				.add("filter")
				.attr("id", this.getId())
				.attr("in", this.getIn())
				.attr("out", this.getOut())
				.add("property")
				.attr("name", "mlt_service")
				.set("filter")
				.up()
				.add("property")
				.attr("name", "mlt_service")
				.set("webvfx")
				.up()
				.add("property")
				.attr("name", "resource")
				.set("/var/www/html/templates")
				.up()
				.add("property")
				.attr("name", "transparent")
				.set("1");

		int width = this.quality.equalsIgnoreCase("hd") ? 1280 : 640;

		filter.append(new Directives()
				.up()
				.add("property")
				.attr("name", "width")
				.set(width));

		for (Map.Entry<String, String> prop : templateProperties.entrySet()) {
			filter.append(new Directives()
					.up()
					.add("property")
					.attr("name", prop.getKey())
					.set(
							(prop.getKey().equalsIgnoreCase(OverlayPropertyType.TEXTAREA.toString())
									? "<![CDATA[]]>" : "") +
							prop.getValue().replaceAll("(\r\n|\n)", "<br />")));
		}

		filter.append(new Directives()
				.up()
				.add("property")
				.attr("name", "clipDuration")
				.set(this.getOut() - this.getIn()));
		
		filter.append(new Directives()
				.up()
				.add("property")
				.attr("name", "fps")
				.set(this.fps));

		return filter;
	}

	public String getHtmlTemplate() {
		return htmlTemplate;
	}

	public void setHtmlTemplate(String htmlTemplate) {
		this.htmlTemplate = htmlTemplate;
	}

	public HashMap<String, String> getTemplateProperties() {
		return templateProperties;
	}

	public void setTemplateProperties(HashMap<String, String> templateProperties) {
		this.templateProperties = templateProperties;
	}

	public void addTemplateProperty(String key, String value) {
		this.templateProperties.put(key, value);
	}
}
