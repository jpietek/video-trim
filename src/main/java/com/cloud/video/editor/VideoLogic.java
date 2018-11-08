package com.cloud.video.editor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;

import com.cloud.video.editor.model.KeyframeSide;
import com.cloud.video.editor.model.Result;
import com.cloud.video.editor.model.Video;
import com.cloud.video.editor.utils.HttpUtils;
import com.cloud.video.editor.utils.Mp4Utils;
import com.cloud.video.editor.utils.StringUtils;

import lombok.extern.java.Log;

@Log
public class VideoLogic {

	private static final VideoLogic INSTANCE = new VideoLogic();

	private static final Executor VIDEO_EXECUTOR = Executors.newFixedThreadPool(128);

	private static final String CHUNKS_DIR = "chunks";

	private VideoLogic() {

	}

	private String prepareBaseDirs() throws IOException {
		final String basepath = "/var/www/html/out/" + StringUtils.getRandomId();
		FileUtils.forceMkdir(new File(basepath));
		FileUtils.forceMkdir(new File(HttpUtils.buildURL(basepath, CHUNKS_DIR)));
		return basepath;
	}

	public Result trimVideo(Set<Video> clips) throws IOException {

		final String basepath = this.prepareBaseDirs();

		List<Result> clipRenderResults = clips.parallelStream().map(c -> {

			final String url = c.getDirectContentLink();
			final double in = c.cutInSeconds();
			final double out = c.cutOutSeconds();

			final String leftPath = HttpUtils.buildURL(basepath, CHUNKS_DIR,
					c.getSortId() + "-left-full.ts");
			final String rightPath = HttpUtils.buildURL(basepath, CHUNKS_DIR,
					c.getSortId() + "-right-full.ts");
			final String leftTrimmedPath = leftPath.replace("-full", "");
			final String rightTrimmedPath = rightPath.replace("-full", "");
			final String middlePath = HttpUtils.buildURL(basepath, CHUNKS_DIR,
					c.getSortId() + "-middle.ts");

			final CompletionStage<Double> inLeftFuture = Mp4Utils.getLeftKeyframe(in, url);
			final CompletionStage<Double> inRightFuture = Mp4Utils.getRightKeyframe(in, url);
			final CompletionStage<Double> outLeftFuture = Mp4Utils.getLeftKeyframe(out, url);
			final CompletionStage<Double> outRightFuture = Mp4Utils.getRightKeyframe(out,
					url);

			final CompletableFuture<Boolean> leftChunkResult = inLeftFuture
					.thenCombineAsync(inRightFuture, (leftKeyframe, rightKeyframe) -> {
						Mp4Utils.extractKeyFramedSegment(leftKeyframe, (Double) rightKeyframe,
								url, leftPath, 0.15);

						final double trimLeftIn = in - leftKeyframe;
						final double segmentDuration = (Double) rightKeyframe - leftKeyframe;
						final double leftDuration = segmentDuration - trimLeftIn;

						return Mp4Utils.trimReencodeSegment(c, trimLeftIn, leftDuration,
								leftPath, leftTrimmedPath, KeyframeSide.LEFT);
					}, VIDEO_EXECUTOR).toCompletableFuture();

			final CompletableFuture<Boolean> rightChunkResult = outLeftFuture
					.thenCombineAsync(outRightFuture, (leftKeyframe, rightKeyframe) -> {
						Mp4Utils.extractKeyFramedSegment(leftKeyframe, (Double) rightKeyframe,
								url, rightPath, 0.15);

						final double trimRightIn = 0;
						final double rightDuration = out - leftKeyframe;

						return Mp4Utils.trimReencodeSegment(c, trimRightIn, rightDuration,
								rightPath, rightTrimmedPath, KeyframeSide.RIGHT);
					}, VIDEO_EXECUTOR).toCompletableFuture();

			final CompletableFuture<Boolean> middleChunkResult = inRightFuture
					.thenCombineAsync(outLeftFuture, (leftKeyframe, rightKeyframe) -> {
						if (leftKeyframe == (double) rightKeyframe) {
							return false;
						}

						final double gopLength = (Double) rightKeyframe - leftKeyframe;
						final double leftOffset = 0.07 * gopLength;

						return Mp4Utils.extractKeyFramedSegment(leftKeyframe,
								(Double) rightKeyframe, url, middlePath, leftOffset);
					}, VIDEO_EXECUTOR).toCompletableFuture();

			final List<String> chunksToConcat = new ArrayList<>();
			try {
				if (leftChunkResult.get()) {
					chunksToConcat.add(leftTrimmedPath);
				}
				if (middleChunkResult.get()) {
					chunksToConcat.add(middlePath);
				}
				if (rightChunkResult.get()) {
					chunksToConcat.add(rightTrimmedPath);
				}
				return Mp4Utils.concatProtocol(chunksToConcat,
						basepath + "/" + c.getSortId() + ".mp4");
			} catch (ExecutionException e) {
				return new Result(false, e.getMessage());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return new Result(false, e.getMessage());
			}
		}).collect(Collectors.toList());

		Optional<Result> failed = clipRenderResults.stream().filter(res -> !res.isSuccess())
				.findFirst();

		if (failed.isPresent()) {
			return new Result(false,
					"one of the clip renders failed, " + failed.get().getMsg());
		}

		if (clips.size() == 1) {
			return clipRenderResults.get(0);
		}

		List<String> chunkPaths = IntStream.range(0, clips.size())
				.mapToObj(n -> HttpUtils.buildURL(basepath, n + ".mp4"))
				.collect(Collectors.toList());

		log.info("chunk paths: " + chunkPaths);
		String mp4Out = basepath + "/out.mp4";
		return Mp4Utils.fileConcat(chunkPaths, mp4Out);
	}

	public static VideoLogic getInstance() {
		return INSTANCE;
	}

}
