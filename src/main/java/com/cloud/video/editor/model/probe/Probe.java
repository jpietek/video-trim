package com.cloud.video.editor.model.probe;

import java.util.ArrayList;
import java.util.List;

public class Probe { 
	 
    private List<Stream> streams = new ArrayList<Stream>(); 
    private Format format;
	public List<Stream> getStreams() {
		return streams;
	}
	public void setStreams(List<Stream> streams) {
		this.streams = streams;
	}
	public Format getFormat() {
		return format;
	}
	public void setFormat(Format format) {
		this.format = format;
	} 
 
}
