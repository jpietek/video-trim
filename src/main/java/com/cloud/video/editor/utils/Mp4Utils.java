package com.cloud.video.editor.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.cloud.video.editor.model.Video;
import com.cloud.video.editor.model.Result;
import com.google.api.client.util.Charsets;

public class Mp4Utils {

	public static Result fileConcat(List<String> inputFiles, String outputFilename) {
		StringBuffer sb = new StringBuffer();
		for (String fileName : inputFiles) {
			sb.append("file '" + fileName + "'" + System.lineSeparator());
		}

		File output = new File(outputFilename + ".txt");
		try {
			FileUtils.writeStringToFile(output, sb.toString(), Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return new Result(false, "exception during writing concat file");
		}

		String cmd = "ffmpeg -y -loglevel panic -f concat -safe 0 -i " + outputFilename + ".txt" + " -c copy "
				+ outputFilename;

		boolean concatResult = SysUtils.getExitCode(cmd);

		if (!concatResult) {
			return new Result(false, "concat ffmpeg command failed");
		}

		String url = outputFilename.replace("/var/www/html", "");
		Video gf = new Video();
		gf.setDirectContentLink(url);
		return new Result(true, "file concat ok", gf);
	}

	public static Result getIFramesNearTimecode(double timeSeconds, String videoPath) {
		double left = timeSeconds - 10;
		double right = timeSeconds + 10;

		String cmd = "ffprobe -loglevel panic -show_frames -select_streams v "
				+ "-show_entries frame=pkt_dts_time,pict_type -print_format csv -read_intervals " 
				+ left + "%" + right + " -i " + videoPath;

		List<String> keyFrameStrings = SysUtils.getResultStream(cmd).filter(line -> line.contains(",I"))
				.collect(Collectors.toList());

		if (keyFrameStrings.size() < 2) {
			return new Result(false, "didn't find at least two keyframes in surrounding 10s");
		}

		double leftDelta = Double.MAX_VALUE;
		Double leftVal = null; Double rightVal = null;
		double rightDelta = Double.MAX_VALUE;
		for (String keyFrameString : keyFrameStrings) {
			try {
				double val = Double.parseDouble(keyFrameString.split(",")[1]);
				System.out.println("parsed val: " + val);
				double delta = val - timeSeconds;
				System.out.println("delta: " + delta);
				if (delta <= 0) {
					if (Math.abs(delta) < leftDelta) {
						leftDelta = Math.abs(delta);
						leftVal = val;
					}
				} else if (delta > 0) {
					if (Math.abs(delta) < rightDelta) {
					    rightDelta = Math.abs(delta);
						rightVal = val;
					}
				}
			} catch (NumberFormatException e) {
				continue;
			}
		}

		if (rightVal == null || leftVal == null) {
			return new Result(false, "can't find 2 keyframes round time: " + timeSeconds + " " + rightVal +  " " + leftVal);
		}

		System.out.println("found keyframes: " + leftVal + " " + rightVal);
		return new Result(true, "keyframes found", Pair.of(leftVal, rightVal));
	}

	public static Result trimReencodeSegment(double in, double duration, String inputPath, double fps, String offsetSide,
			String outputPath) {

		if (offsetSide.equalsIgnoreCase("left")) {
			duration -= ((1 / fps) * 1.5);
			in += 0.05;
			
		} else if (offsetSide.equalsIgnoreCase("right")) {
			in += ((1 / fps) * 1.5);
			duration += 0.05;
		}

		String cmd = "ffmpeg -y -loglevel panic -i " + inputPath + " -ss " + in + " -t " + duration + " -vcodec libx264 " + outputPath;

		boolean trimResult = SysUtils.getExitCode(cmd);

		if (!trimResult) {
			return new Result(false, "trimming video chunk failed, " + inputPath + " " + in + " " + duration);
		}

		System.out.println("trim res: " + trimResult);
		return new Result(true, "trim with reencode ok");
	}
	
	public static Result reencodeSingleSegment(String inputPath, double in, double out, String outputPath) {
		String cmd = "ffmpeg -y -loglevel panic -i " + inputPath + " -ss " + in + " -to " + out + " -vcodec libx264 " + outputPath;
		
		boolean trimResult = SysUtils.getExitCode(cmd);

		if (!trimResult) {
			return new Result(false, "trimming video chunk failed, " + inputPath + " " + in + " " + out);
		}

		String url = outputPath.replace("/var/www/html", "");
		Video gf = new Video();
		gf.setDirectContentLink(url);
		return new Result(true, "file concat ok", gf);
	}

	public static Result extractKeyFramedSegment(double in, double out, String inputPath, String outputPath,
			double fps) {

		final double offset = 1 / fps * 2.0;

		final double seekIn = in + offset;
		final double duration = out - in - offset;
		String cmd = "ffmpeg -noaccurate_seek -y -loglevel panic -ss " + seekIn + " -i " + inputPath
				+ " -codec copy -t " + duration + " -avoid_negative_ts make_zero -fflags +genpts " + outputPath;

		boolean trimResult = SysUtils.getExitCode(cmd);

		if (!trimResult) {
			return new Result(false, "extracting keyframed video chunk failed, " + inputPath + " " + in + " " + out);
		}
		System.out.println("trim res: " + trimResult);
		
		
		return new Result(true, "keyframed segment extraction ok");
	}

}
