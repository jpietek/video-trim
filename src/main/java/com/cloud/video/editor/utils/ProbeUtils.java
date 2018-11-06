package com.cloud.video.editor.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.apache.tomcat.util.http.fileupload.FileUtils;

import com.cloud.video.editor.model.Result;
import com.cloud.video.editor.model.probe.Probe;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.java.Log;

@Log
public class ProbeUtils {

	private ProbeUtils() {

	}

	public static Result probeVideo(String videoPath) {
		final String[] cmds = { "ffprobe", "-v", "quiet", "-print_format", "json",
				"-show_format", "-show_streams", videoPath };

		try {
			ProcessBuilder pb = new ProcessBuilder(cmds);
			Process proc = pb.start();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(proc.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
			}
			String json = builder.toString();

			ObjectMapper mapper = new ObjectMapper();
			Probe p = mapper.readValue(json, Probe.class);

			proc.waitFor();

			return (proc.exitValue() == 0) ? new Result(true, "ffprobe ok", p)
					: new Result(false, "ffprobe failed, invalid media file?");

		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			return new Result(false, "ffprobe failed");
		} finally {
			try {
				FileUtils.forceDelete(new File(videoPath));
			} catch (IOException e) {
				log.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}
}
