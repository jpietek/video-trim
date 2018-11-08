package com.cloud.video.editor;

import java.util.List;

import com.cloud.video.editor.model.Result;
import com.cloud.video.editor.model.Video;

public interface CloudVideoSource {

	public Result getDirectLink(Video video);
	public List<Video> getVideos(String thumbSize);
	
}
