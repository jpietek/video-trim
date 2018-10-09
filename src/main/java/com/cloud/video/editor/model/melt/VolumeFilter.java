package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public class VolumeFilter extends Filter implements FilterInterface {

	private double startGain;
	private double endGain;
	
	public VolumeFilter(int in, int out, double startGain, double endGain) {
		super();
		this.setIn(in);
		this.setOut(out);
		this.startGain = startGain;
		this.endGain = endGain;
	}

	public double getStartGainPercentage() {
		return startGain;
	}

	public void setStartGainPercentage(int startGainPercentage) {
		this.startGain = startGainPercentage;
	}

	public double getEndGainPercentage() {
		return endGain;
	}

	public void setEndGainPercentage(int endGainPercentage) {
		this.endGain = endGainPercentage;
	}

	@Override
	public Directives toXml() {
		Directives filter = new Directives()
				.add("filter")
				.attr("in", this.getIn())
				.attr("out", this.getOut())
				.add("property")
				.attr("name", "mlt_type")
				.set("filter")
				.up()
				.add("property")
				.attr("name", "mlt_service")
				.set("volume")
				.up()
				.add("property")
				.attr("name", "gain")
				.set(this.startGain)
				.up()
				.add("property")
				.attr("name", "end")
				.set(this.endGain)
				.up();
		return filter;
	}
}
