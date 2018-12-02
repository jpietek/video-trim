package com.cloud.video.editor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.cloud.video.editor.model.*;
import com.cloud.video.editor.utils.VideoFunctions;
import org.apache.commons.io.FileUtils;

import com.cloud.video.editor.utils.HttpUtils;
import com.cloud.video.editor.utils.MediaUtils;
import com.cloud.video.editor.utils.StringUtils;

import lombok.extern.java.Log;

@Log
class VideoLogic {

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

    Result trimVideo(Set<Video> clips) throws IOException {

        final String basepath = this.prepareBaseDirs();
        final String chunksBasepath = HttpUtils.buildURL(basepath, CHUNKS_DIR);

        List<Result> clipRenderResults = clips.parallelStream().map(c -> {
            c.setBasepath(chunksBasepath);

            final ExtractKeyframeRequest leftReq
                    = ExtractKeyframeRequest.builder().side(KeyframeSide.LEFT).video(c).build();
            final ExtractKeyframeRequest rightReq
                    = ExtractKeyframeRequest.builder().side(KeyframeSide.RIGHT).video(c).build();

            final CompletionStage<ExtractKeyframeResult> inLeftFuture = MediaUtils.getLeftKeyframe(leftReq);
            final CompletionStage<ExtractKeyframeResult> inRightFuture = MediaUtils.getRightKeyframe(leftReq);
            final CompletionStage<ExtractKeyframeResult> outLeftFuture = MediaUtils.getLeftKeyframe(rightReq);
            final CompletionStage<ExtractKeyframeResult> outRightFuture = MediaUtils.getRightKeyframe(rightReq);

            final CompletableFuture<Optional<String>> leftChunkResult = inLeftFuture
                    .thenCombineAsync(inRightFuture, VideoFunctions.leftTrimmer, VIDEO_EXECUTOR).toCompletableFuture();

            final CompletableFuture<Optional<String>> rightChunkResult = outLeftFuture
                    .thenCombineAsync(outRightFuture, VideoFunctions.rightTrimmer, VIDEO_EXECUTOR).toCompletableFuture();

            final CompletableFuture<Optional<String>> middleChunkResult = inRightFuture
                    .thenCombineAsync(outLeftFuture, VideoFunctions.middleTrimmer, VIDEO_EXECUTOR).toCompletableFuture();

                final List<String> chunksToConcat
                        = Stream.of(leftChunkResult, middleChunkResult, rightChunkResult)
                        .map(CompletableFuture::join)
                        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

                return MediaUtils.concatProtocol(chunksToConcat,
                        HttpUtils.buildURL(basepath, c.getSortId() + ".mp4"));
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
                .mapToObj(n -> HttpUtils.buildURL(chunksBasepath, n + ".mp4"))
                .collect(Collectors.toList());

        log.info("chunk paths: " + chunkPaths);
        String mp4Out = HttpUtils.buildURL(chunksBasepath, "out.mp4");
        return MediaUtils.fileConcat(chunkPaths, mp4Out);
    }

    static VideoLogic getInstance() {
        return INSTANCE;
    }

}
