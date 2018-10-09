package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public class AudioTransition extends MeltTransition implements MeltTransitionInterface {

	int index;
	
	public AudioTransition(int id) {
		super("transition" + id);
		this.index = id;
	}

	public AudioTransition(int id, int length) {
		super("transition" + id, length);
		this.index = id;
	}

	public Directives toXml() {
		Directives transition = new Directives()
				.add("transition")
				.attr("id", this.getId());
		if(this.getLength() != null) {
			transition.append(new Directives().attr("out", this.getLength()));
		}
		transition.append(new Directives()
				.add("property")
				.attr("id", "a_track")
				.set("0")
				.up()
				.add("property")
				.attr("id", "b_track")
				.set(index + 1)
				.up()
				.add("property")
				.attr("name", "mlt_type")		
				.set("transition")
				.up()
				.add("property")
				.attr("name", "mlt_service")
				.set("mix")
				.up()
				.add("property")
				.attr("name", "combine")
				.set("0"));
		return transition;
	}	
}
