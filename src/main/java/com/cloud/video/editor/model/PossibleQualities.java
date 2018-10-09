package com.cloud.video.editor.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PossibleQualities {
	public static final Map<String, VideoQuality> qualities;

	static {
		Map<String, VideoQuality> m = new HashMap<String, VideoQuality>();
		
		m.put("low", 	new VideoQuality("low", 	320, 180, 		270000L));
		m.put("med", 	new VideoQuality("med", 	480, 270, 		700000L));
		m.put("hi", 	new VideoQuality("hi", 		640, 360, 		1500000L));
		m.put("hd", 	new VideoQuality("hd", 		1280, 720, 		4000000L));
		m.put("fhd", 	new VideoQuality("fhd", 	1920, 1080, 	8000000L));
		m.put("qhd", 	new VideoQuality("qhd", 	2560, 1440, 	12000000L));
		m.put("uhd", 	new VideoQuality("uhd", 	3840, 2160, 	16000000L));
		
		qualities = Collections.unmodifiableMap(m);
	}
}