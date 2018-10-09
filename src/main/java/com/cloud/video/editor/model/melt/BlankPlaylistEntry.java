package com.cloud.video.editor.model.melt;

import org.xembly.Directives;

public class BlankPlaylistEntry extends PlaylistEntry {

	private int length;

	public BlankPlaylistEntry(int length) {
		super();
		this.length = length;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public Directives toXml() {		
		Directives blank = new Directives()
				.add("blank")
				.attr("length", this.length);

		return blank;
	}

}
