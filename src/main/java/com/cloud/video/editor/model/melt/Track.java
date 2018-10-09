package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public class Track {
	private String id;
	private Integer in;
	private Integer out;
	private String producer;
	private String hide;
	
	public Track(int id, int in, int out, String producer) {
		this.id = "track" + id;
		this.in = in;
		this.out = out;
		this.producer = producer;
	}
	
	public Track(int in, int out, String customProducer) {
		this.in = in;
		this.out = out;
		this.producer = customProducer;
	}

	public Track(int id, String producer) {
		this.producer = producer;
	}
	
	public Track(int out) {
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
	public String getProducer() {
		return producer;
	}
	public void setProducer(String producer) {
		this.producer = producer;
	}

	public String getHide() {
		return hide;
	}

	public void setHide(String hide) {
		this.hide = hide;
	}

	public Directives toXml() {		
		Directives track = new Directives()
			.add("track");
		
		if(this.producer != null) {
			track.attr("producer", this.producer);
		}
				
		if(this.id != null) {
			track.attr("id", this.id);
		}
		
		if(this.in != null) {
			track.attr("in", this.in);
			
		}
		
		if(this.out != null) {
			track.attr("out", this.out);
		}
		
		if(this.hide != null) {
			track
			.attr("hide", this.hide);
		}
		return track;
	}
}
