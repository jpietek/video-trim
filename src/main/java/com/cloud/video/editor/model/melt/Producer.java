package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public class Producer implements ProducerInterface {

	private String videoPath;
	private String id;
	private Integer in;
	private Integer out;
	private boolean loop;
	private String type = "avformat";
	private Double speed = 1.0;

	public Producer() {
	}
	
	public Producer(String id, String mediaPath) {
		this.id = id;
		this.videoPath = mediaPath;
	}

	public Producer(String id, String videoPath, Integer in, Integer out) {
		this.id = id;
		this.videoPath = videoPath;
		this.in = in;
		this.out = out;
	}

	public String getIdString() {
		return this.id;
	}

	public Directives toXml() {	
		Directives producer = new Directives()
				.xpath("/mlt/producers")
				.add("producer")
				.attr("id", this.getIdString());
		
		if(this.in != null && this.out != null) {
			producer.append(new Directives()
					.attr("in", this.in)
					.attr("out", this.out));
		}

		producer
			.add("property")
			.attr("name", "mlt_type")
			.set("producer")
			.up()
			.add("property")
			.attr("name", "mlt_service")
			.set(type)
			.up()
			.add("property")
			.attr("name", "resource")
			.set(this.videoPath);

		if(this.videoPath.endsWith(".m3u8")) {
			producer
			.up()
			.add("property")
			.attr("name", "fflags")
			.set("+nofillin");
		}
		
		if(this.loop) {
			producer
			.up()
			.add("property")
			.attr("name", "eof")
			.set("loop");
		}

		return producer;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVideoPath() {
		return videoPath;
	}
	
	public boolean isLoop() {
		return loop;
	}

	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}

	public Double getSpeed() {
		return speed;
	}

	public void setSpeed(Double speed) {
		this.speed = speed;
	}

	@Override
	public Integer getIn() {
		return this.in;
	}

	@Override
	public Integer getOut() {
		return this.out;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
