package com.cloud.video.editor.utils;

import com.cloud.video.editor.model.ChunkType;
import com.cloud.video.editor.model.ExtractKeyframeResult;
import com.cloud.video.editor.model.KeyframeSide;
import com.cloud.video.editor.model.Video;

import java.util.Optional;
import java.util.function.BiFunction;

public class VideoFunctions
{
    public static BiFunction<ExtractKeyframeResult, ExtractKeyframeResult, Optional<String>> leftTrimmer
            = (leftResult, rightResult) -> {
        final double leftKeyframe = leftResult.getTime();
        final double rightKeyframe = rightResult.getTime();
        final Video video = leftResult.getVideo();
        final String leftPath = video.getFullSegmentPath(ChunkType.LEFT);
        final String leftTrimmedPath = video.getTrimmedSegmentPath(ChunkType.LEFT);
        MediaUtils.extractKeyFramedSegment(leftKeyframe, rightKeyframe,
                video.getDirectContentLink(), leftPath, 0.15);

        final double trimLeftIn = video.getCutIn() - leftKeyframe;
        final double segmentDuration = rightKeyframe - leftKeyframe;
        final double leftDuration = segmentDuration - trimLeftIn;

        return MediaUtils.trimReencodeSegment(video, trimLeftIn, leftDuration,
                leftPath, leftTrimmedPath, KeyframeSide.LEFT);
    };

    public static BiFunction<ExtractKeyframeResult, ExtractKeyframeResult, Optional<String>> middleTrimmer
            = (leftResult, rightResult) -> {
        final double leftKeyframe = leftResult.getTime();
        final double rightKeyframe = rightResult.getTime();
        final Video video = leftResult.getVideo();

        if (leftKeyframe == (double) rightKeyframe) {
            return Optional.empty();
        }

        final double gopLength = rightKeyframe - leftKeyframe;
        final double leftOffset = 0.07 * gopLength;

        return MediaUtils.extractKeyFramedSegment(leftKeyframe,
                rightKeyframe, video.getFullSegmentPath(ChunkType.MIDDLE),
                video.getTrimmedSegmentPath(ChunkType.MIDDLE), leftOffset);
    };

    public static BiFunction<ExtractKeyframeResult, ExtractKeyframeResult, Optional<String>> rightTrimmer
            = (leftResult, rightResult) -> {
        final double leftKeyframe = leftResult.getTime();
        final double rightKeyframe = rightResult.getTime();
        final Video video = leftResult.getVideo();
        final String rightPath = video.getFullSegmentPath(ChunkType.RIGHT);
        final String rightTrimmedPath = video.getTrimmedSegmentPath(ChunkType.RIGHT);
        MediaUtils.extractKeyFramedSegment(leftKeyframe, rightKeyframe,
                video.getDirectContentLink(), rightPath, 0.15);

        final double trimRightIn = 0;
        final double rightDuration = video.getCutOut() - leftKeyframe;

        return MediaUtils.trimReencodeSegment(video, trimRightIn, rightDuration,
                rightPath, rightTrimmedPath, KeyframeSide.RIGHT);
    };
}
