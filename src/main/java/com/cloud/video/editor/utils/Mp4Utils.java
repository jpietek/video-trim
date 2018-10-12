package com.cloud.video.editor.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.cloud.video.editor.model.Video;
import com.cloud.video.editor.model.Result;
import com.google.api.client.util.Charsets;

public class Mp4Utils {

	enum KeyframeSide {
		LEFT, RIGHT
	}

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

	public static Result getIFramesNearTimecodeFast(double seekPoint, String videoPath) {

		CompletableFuture<Double> leftKeyframeFuture = CompletableFuture.supplyAsync(() -> {
			for (int i = 0; i < 10; i++) {
				System.out.println("left keyframe iter: " + i);
				double left = seekPoint - 2 * (i + 1);
				double right = seekPoint - 2 * i;
				List<String> keyframes = getKeyframes(left, right, videoPath);
				if (keyframes.isEmpty()) {
					continue;
				} else {
					Double closestKeyframe = getClosestKeyFrame(keyframes, seekPoint, KeyframeSide.LEFT);
					if(closestKeyframe != null) {
						return closestKeyframe;
					}
				}
			}
			return null;
		});

		CompletableFuture<Double> rightKeyframeFuture = CompletableFuture.supplyAsync(() -> {
			for (int i = 0; i < 10; i++) {
				System.out.println("right keyframe iter: " + i);
				double left = seekPoint + 2 * i;
				double right = seekPoint + 2 * (i + 1);
				List<String> keyframes = getKeyframes(left, right, videoPath);
				if (keyframes.isEmpty()) {
					continue;
				} else {
					Double closestKeyframe = getClosestKeyFrame(keyframes, seekPoint, KeyframeSide.RIGHT);
					if(closestKeyframe != null) {
						return closestKeyframe;
					}
				}
			}
			return null;
		});
		
		Double leftKeyFrame = leftKeyframeFuture.join();
		Double rightKeyFrame = rightKeyframeFuture.join();
		
		if (leftKeyFrame == null || rightKeyFrame == null) {
			return new Result(false, "could not find keyframes in +/- 20 seconds interval around seek point");
		} else {
			System.out.println("found keyframes: " + leftKeyFrame + " " + rightKeyFrame);
			return new Result(true, "keyframes found", Pair.of(leftKeyFrame, rightKeyFrame));
		}
	}

	public static List<String> getKeyframes(double left, double right, String videoPath) {
		String cmd = "ffprobe -loglevel panic -show_frames -select_streams v "
				+ "-show_entries frame=pkt_dts_time,pict_type -print_format csv -read_intervals " + left + "%" + right
				+ " -i " + videoPath;

		return SysUtils.getResultStream(cmd).filter(line -> line.contains(",I") && !line.contains("N/A"))
				.collect(Collectors.toList());
	}

	public static double getClosestKeyFrame(List<String> keyframes, double seekPoint, KeyframeSide side) {
		double minDelta = Double.MAX_VALUE;
		Double closest = null;
		for (String keyFrameString : keyframes) {
			double val = Double.parseDouble(keyFrameString.split(",")[1]);
			System.out.println("parsed val: " + val);
			double delta = val - seekPoint;

			if (side == KeyframeSide.LEFT && delta > 0) {
				continue;
			} else if (side == KeyframeSide.RIGHT && delta <= 0) {
				continue;
			}

			System.out.println("delta: " + delta);
			if (Math.abs(delta) < minDelta) {
				minDelta = Math.abs(delta);
				closest = val;
			}
		}

		return closest;
	}

	public static Result trimReencodeSegment(double in, double duration, String inputPath, double fps,
			String offsetSide, String outputPath) {

		if (offsetSide.equalsIgnoreCase("left")) {
			duration -= ((1 / fps) * 1.5);
			in += 0.05;

		} else if (offsetSide.equalsIgnoreCase("right")) {
			in += ((1 / fps) * 1.5);
			duration += 0.05;
		}

		String cmd = "ffmpeg -y -loglevel panic -i " + inputPath + " -ss " + in + " -t " + duration
				+ " -vcodec libx264 " + outputPath;

		boolean trimResult = SysUtils.getExitCode(cmd);

		if (!trimResult) {
			return new Result(false, "trimming video chunk failed, " + inputPath + " " + in + " " + duration);
		}

		System.out.println("trim res: " + trimResult);
		return new Result(true, "trim with reencode ok");
	}

	public static Result reencodeSingleSegment(String inputPath, double in, double out, String outputPath) {
		String cmd = "ffmpeg -y -loglevel panic -i " + inputPath + " -ss " + in + " -to " + out + " -vcodec libx264 "
				+ outputPath;

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
