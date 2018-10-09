package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public class SlowMotionProducer extends Producer implements ProducerInterface {

	private String videoPath;
	private String id;
	private Integer in;
	private Integer out;
	private Double speed = 1.0;
	private Double fps;
	
	public SlowMotionProducer(String id, Integer in, Integer out, String mediaPath, Double speed, Double fps) {
		this.id = id;
		this.videoPath = mediaPath;
		this.speed = speed;
		this.out = out;
		this.in = in;
		this.fps =  fps;
		this.setLoop(false);
	}
	
	public Directives toXml() {	
		Directives producer = new Directives()
				.xpath("/mlt/producers")
				.add("producer")
				.attr("id", this.id);
		
		if(this.in != null) {
			producer.append(new Directives()
					.attr("in", this.in));
		}
		
		producer.append(new Directives()
			.attr("out", this.out));
		
		producer.append(new Directives()
				.attr("speed", this.speed));
		
		producer
		.add("property")
		.attr("name", "resource")
		//.set(this.speed + ":" + this.videoPath)
		.set(this.videoPath);
		
		if(this.videoPath.endsWith(".m3u8")) {
			producer
			.up()
			.add("property")
			.attr("name", "fflags")
			.set("+nofillin");
		}
		
		producer
		.up()
		.add("property")
		.attr("name", "mlt_type")
		.set("producer")
		.up()
		.add("property")
		.attr("name", "mlt_service")
		.set("timewarp")
		.up()
		.add("property")
		.attr("name", "force_fps")
		.set(this.fps * this.speed)
		.up()
		.add("property")
		.attr("name", "length")
		.set(out);
		
		return producer;
	}

	public String getVideoPath() {
		return videoPath;
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getIn() {
		return in;
	}

	public void setIn(Integer in) {
		this.in = in;
	}

	public Integer getOut() {
		return out;
	}

	public void setOut(Integer out) {
		this.out = out;
	}

	public Double getSpeed() {
		return speed;
	}

	public void setSpeed(Double speed) {
		this.speed = speed;
	}
	
}
