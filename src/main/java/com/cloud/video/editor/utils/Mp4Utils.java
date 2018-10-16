package com.cloud.video.editor.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.cloud.video.editor.model.KeyframeSide;
import com.cloud.video.editor.model.Result;
import com.cloud.video.editor.model.Video;
import com.cloud.video.editor.model.probe.Probe;
import com.cloud.video.editor.model.probe.Stream;
import com.google.api.client.util.Charsets;

public class Mp4Utils {

	private static final Executor mp4UtilsExecutor = Executors.newFixedThreadPool(64);

	public static Result mkvMerge(List<String> inputFiles, String outputFileName) {
		StringBuffer cmd = new StringBuffer();
		cmd.append("mkvmerge -o " + outputFileName + " ");
		for (int i = 0; i < inputFiles.size(); i++) {
			cmd.append(inputFiles.get(i) + (i != inputFiles.size() - 1 ? " + " : ""));
		}

		SysUtils.getExitCode(cmd.toString());

		String url = outputFileName.replace("/var/www/html", "");
		Video gf = new Video();
		gf.setDirectContentLink(url);
		return new Result(true, "file concat ok", gf);
	}

	public static Result extractVideo(String inputFile) {
		String h264File = inputFile.replace("mkv", "h264");
		String cmd = "ffmpeg -i " + inputFile + " -vcodec copy " + h264File;

		boolean h264Res = SysUtils.getExitCode(cmd);

		if (!h264Res) {
			return new Result(false, "ffmpeg h264 extract failed");
		}

		return new Result(true, "video extraction ok", h264File);
	}

	public static Result extractAudio(String inputFile) {
		String aacFile = inputFile.replace("mkv", "aac");
		String cmd = "ffmpeg -i " + inputFile + " -acodec copy " + aacFile;

		boolean aacRes = SysUtils.getExitCode(cmd);

		if (!aacRes) {
			return new Result(false, "ffmpeg aac extract failed");
		}

		return new Result(true, "audio extraction ok", aacFile);
	}

	public static Result remuxMkvToMp4(String inputFile, String outputFilename) {

		extractVideo(inputFile);
		extractAudio(inputFile);

		String cmd = "ffmpeg -i " + inputFile.replace("mkv", "h264") + " -i " + inputFile.replace("mkv", "aac")
				+ " -shortest -c copy " + outputFilename;

		boolean muxResult = SysUtils.getExitCode(cmd);

		if (!muxResult) {
			return new Result(false, "mux ffmpeg command failed");
		}

		String url = outputFilename.replace("/var/www/html", "");
		Video gf = new Video();
		gf.setDirectContentLink(url);
		return new Result(true, "remux to mp4 ok", gf);
	}

	public static Result concatProtocol(List<String> inputFiles, String outputFilename) {
		StringBuffer cmd = new StringBuffer();
		cmd.append("/vt/video_trim ");

		for (String input : inputFiles) {
			cmd.append(input + " ");
		}

		cmd.append(outputFilename);

		boolean concatResult = SysUtils.getRuntimeExitCode(cmd.toString());
		if (!concatResult) {
			return new Result(false, "concat protocol ffmpeg command failed");
		}

		String url = outputFilename.replace("/var/www/html", "");
		Video gf = new Video();
		gf.setDirectContentLink(url);
		return new Result(true, "concat protocol ok", gf);
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
			double left = seekPoint;
			double right = seekPoint;
			for (int i = 1; i <= 7; i++) {
				System.out.println("left keyframe iter: " + i);
				right = left;
				left -= Math.pow(2, i);
				List<String> keyframes = getKeyframes(left, right, videoPath);
				if (keyframes.isEmpty()) {
					continue;
				} else {
					Double closestKeyframe = getClosestKeyFrame(keyframes, seekPoint, KeyframeSide.LEFT);
					if (closestKeyframe != null) {
						return closestKeyframe;
					}
				}
			}
			return null;
		}, mp4UtilsExecutor);

		CompletableFuture<Double> rightKeyframeFuture = CompletableFuture.supplyAsync(() -> {
			double left = seekPoint;
			double right = seekPoint;
			for (int i = 1; i <= 7; i++) {
				System.out.println("right keyframe iter: " + i);
				left = right;
				right += Math.pow(2, i);
				List<String> keyframes = getKeyframes(left, right, videoPath);
				if (keyframes.isEmpty()) {
					continue;
				} else {
					Double closestKeyframe = getClosestKeyFrame(keyframes, seekPoint, KeyframeSide.RIGHT);
					if (closestKeyframe != null) {
						return closestKeyframe;
					}
				}
			}
			return null;
		}, mp4UtilsExecutor);

		Double leftKeyFrame = leftKeyframeFuture.join();
		Double rightKeyFrame = rightKeyframeFuture.join();

		if (leftKeyFrame == null || rightKeyFrame == null) {
			return new Result(false, "could not find keyframes in +/- 128 seconds interval around seek point");
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

	public static Double getClosestKeyFrame(List<String> keyframes, double seekPoint, KeyframeSide side) {
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

	public static Result trimReencodeSegment(Video v, double in, double duration, String inputPath, String outputPath,
			KeyframeSide side) {
		double fps = v.getFps();

		if (side == KeyframeSide.LEFT) {
			duration -= ((1 / fps) * 1.5);
			in += 0.05;

		} else if (side == KeyframeSide.RIGHT) {
			in += ((1 / fps) * 1.5);
			duration += 0.05;
		}

		String cmd = "ffmpeg -y -loglevel panic -i " + inputPath + " -ss " + in + " -t " + duration
				+ " -c:v libx264 -profile " + v.getProfile() + " -level " + v.getLevel() + " -b:v "
				+ v.getVideoBitrate() + " -pix_fmt " + v.getPixFormat() + " -c:a aac -b:a " + v.getAudioBitrate()
				+ " " + outputPath;
		
		boolean trimResult = SysUtils.getExitCode(cmd);

		if (!trimResult) {
			return new Result(false, "trimming video chunk failed " + in + " " + duration);
		}

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

	public static Result extractKeyFramedSegment(double in, double out, String inputPath, String outputPath, double fps,
			double offset) {

		final double seekIn = in + offset;
		final double duration = out - in - offset - 0.1;
		String cmd = "ffmpeg -noaccurate_seek -y -loglevel panic -ss " + seekIn + " -i " + inputPath
				+ " -codec copy -t " + duration + " " + outputPath;

		boolean trimResult = SysUtils.getExitCode(cmd);

		if (!trimResult) {
			return new Result(false, "extracting keyframed video chunk failed, " + inputPath + " " + in + " " + out);
		}

		return new Result(true, "keyframed segment extraction ok");
	}

}
