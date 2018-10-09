package com.cloud.video.editor.model.melt;

import java.util.HashMap;

public class OverlayClip {

	private int inSecond;
	private int outSecond;
	private int fps;

	private String htmlTemplate;
	private HashMap<String, String> templateProperties;
	
	public OverlayClip() {
		
	}
	
	public OverlayClip(int inSecond, int outSecond, int fps, String htmlTemplate, 
			HashMap<String, String> templateProperties) {
		this.inSecond = inSecond;
		this.outSecond = outSecond;
		this.fps = fps;
		this.htmlTemplate = htmlTemplate;
		this.templateProperties = templateProperties;
	}
	
	public int getInSecond() {
		return inSecond;
	}

	public void setInSecond(int inSecond) {
		this.inSecond = inSecond;
	}

	public int getOutSecond() {
		return outSecond;
	}

	public void setOutSecond(int outSecond) {
		this.outSecond = outSecond;
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
	public int getFps() {
		return fps;
	}

	public void setFps(int fps) {
		this.fps = fps;
	}
}
