package com.cloud.video.editor.logic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import com.cloud.video.editor.model.Compilation;
import com.cloud.video.editor.model.Result;
import com.cloud.video.editor.model.Video;
import com.cloud.video.editor.model.VideoQuality;
import com.cloud.video.editor.model.melt.LivePreviewParams;
import com.cloud.video.editor.model.melt.Profile;
import com.google.common.base.Charsets;

@Service
public class PreviewLogic {

	private final static Logger LOGGER = Logger.getLogger(PreviewLogic.class.getName());

	private LivePreviewLogic livePreviewLogic;

	private Result setupMltProfile(VideoQuality q, Compilation c) {

		int num = 16;
		int den = 9;
		int height = q.getHeight();
		int width = q.getWidth();

		Profile p = new Profile(width, height, num, den, 16, 9);

		String profilePath = "/data/previews/profile";
		try {
			FileUtils.write(new File(profilePath), p.toString(), Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return new Result(false, "cannot write custom profile");
		}
		return new Result(true, "cannot write custom profile", profilePath);
	}

	public Result playMultiPreview(Compilation c, long websocketUserId, String authToken,
			boolean firstPreview) {

		VideoQuality previewQuality = new VideoQuality("hi");

		Result xmlResult = saveMeltXml(c, previewQuality);

		String xmlPath = (String) xmlResult.getResult();
		LOGGER.info("mlt session xml path: " + xmlPath);

		Result res = this.setupMltProfile(previewQuality, c);
		if (!res.isSuccess()) {
			return new Result(false, "writing custom preview mlt profile failed");
		}

		String customProfile = "/data/previews/profile";

		long bitrate = previewQuality.bandwidth;
		int fps = 25;

		int durationInFrames = (int) Math.ceil(c.getDuration() * fps);
		LivePreviewParams lpp = new LivePreviewParams(customProfile, bitrate, fps,
				durationInFrames, FilenameUtils.getName(xmlPath), 2);

		LOGGER.info("play duration: " + lpp.getDuration());
		Result playResult = livePreviewLogic.play(FilenameUtils.getFullPath(xmlPath),
				c.getId(), websocketUserId, authToken, lpp);
		if (!playResult.isSuccess()) {
			return playResult;
		}
		String previewUrl = (String) playResult.getResult();

		return new Result(true, "preview ok", previewUrl);

	}

	public Result saveMeltXml(Compilation c, VideoQuality quality) {
		MeltXmlBuilder mltBuilder = new MeltXmlBuilder(new ArrayList<Video>(c.getVideos()),
				quality, c.getId());
		return mltBuilder.saveFile();
	}

}
