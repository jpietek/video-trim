package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public class LumaTransition extends MeltTransition implements MeltTransitionInterface {
	
	public LumaTransition(int id, int length) {
		super("transition" + id, length);
	}

	public Directives toXml() {
		Directives transition = new Directives()
				.add("transition")
				.attr("id", this.getId())
				.attr("out", this.getLength())
				.add("property")
				.attr("id", "a_track")
				.set("0")
				.up()
				.add("property")
				.attr("id", "b_track")
				.set("1")
				.up()
				.add("property")
				.attr("name", "factory")
				.set("loader")
				.up()
				.add("property")
				.attr("name", "mlt_type")
				.set("transition")
				.up()
				.add("property")
				.attr("name", "mlt_service")
				.set("luma");
		return transition;
	}
}
