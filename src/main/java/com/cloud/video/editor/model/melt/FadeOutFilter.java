package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public class FadeOutFilter extends Filter implements FilterInterface {	
	double startGain = 1.0;
	double endGain = 0.0;
	
	public FadeOutFilter(int id, int in, int out, double startGain, double endGain) {
		this.setId(id);
		this.setIn(in);
		this.setOut(out);
		this.startGain = startGain;
		this.endGain = endGain;
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
				.attr("name", "mlt_type")
				.set("filter")
				.up()
				.add("property")
				.attr("name", "mlt_service")
				.set("volume")
				.up()
				.add("property")
				.attr("name", "track")
				.set("0")
				.up()
				.add("property")
				.attr("name", "gain")
				.set(this.startGain)
				.up()
				.add("property")
				.attr("name", "end")
				.set(this.endGain);
			return filter;
	}
}
