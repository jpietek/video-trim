package com.cloud.video.editor.model.probe;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Probe {

	private List<Stream> streams = new ArrayList<>();
	private Format format;

}
