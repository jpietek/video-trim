package com.cloud.video.editor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;

import com.cloud.video.editor.model.Result;
import com.cloud.video.editor.model.Video;
import com.cloud.video.editor.model.probe.Probe;
import com.cloud.video.editor.model.probe.Stream;
import com.cloud.video.editor.utils.ProbeUtils;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetTemporaryLinkResult;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.MediaMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.ThumbnailFormat;
import com.dropbox.core.v2.files.ThumbnailSize;
import com.dropbox.core.v2.files.VideoMetadata;
import com.dropbox.core.v2.sharing.AccessLevel;
import com.dropbox.core.v2.sharing.FileMemberActionResult;
import com.dropbox.core.v2.sharing.MemberSelector;
import com.dropbox.core.v2.users.FullAccount;

import lombok.extern.java.Log;

@Log
public class DropboxLogic {

	private DbxClientV2 client;

	public DropboxLogic(String accessToken) {
		DbxRequestConfig config = DbxRequestConfig.newBuilder("video-manager/0.1").build();
		client = new DbxClientV2(config, accessToken);
	}

	public Result getDirectLink(Video vf) {
		try {
			GetTemporaryLinkResult res = client.files().getTemporaryLink(vf.getPath());
			String path = res.getLink();
			vf.setDirectContentLink(path);
			Result probeRes = ProbeUtils.probeVideo(path);
			if (!probeRes.isSuccess()) {
				new Result(false, "ffprobe for given video failed");
			}

			Probe p = (Probe) probeRes.getResult();
			Optional<Stream> videoStreamOpt = p.getStreams().stream()
					.filter(s -> s.getCodec_type().equalsIgnoreCase("video")).findFirst();

			if (!videoStreamOpt.isPresent()) {
				new Result(false, "no video present in given file");
			}

			Optional<Stream> audioStreamOpt = p.getStreams().stream()
					.filter(s -> s.getCodec_type().equalsIgnoreCase("video")).findFirst();
			if (!audioStreamOpt.isPresent()) {
				new Result(false, "no audio present in given file");
			}

			if (audioStreamOpt.isPresent()) {
				Stream audio = audioStreamOpt.get();
				vf.setAudioCodecName(audio.getCodec_name());
				vf.setAudioBitrate(256000);
			}

			if (videoStreamOpt.isPresent()) {
				Stream video = videoStreamOpt.get();
				String fpsString = video.getAvg_frame_rate();

				if (fpsString == null) {
					return new Result(false, "can't determing video fps");
				}

				int fpsNum = Integer.parseInt(fpsString.split("\\/")[0]);
				int fpsDenum = Integer.parseInt(fpsString.split("\\/")[1]);
				double fps = 1.0 * fpsNum / fpsDenum;
				log.info("fps: " + fps);
				vf.setFps(fps);
				vf.setFrameCount(video.getNb_frames());
				vf.setVideoBitrate(video.getBit_rate());
				vf.setVideoCodecNAme(video.getCodec_name());
				vf.setProfile(video.getProfile());
				vf.setLevel(String.valueOf(video.getLevel()));
				vf.setPixFormat(video.getPix_fmt());
			} else {
				return new Result(false, "video stream not present");
			}

		} catch (DbxException e) {
			new Result(false, "dropbox exception while getting a direct link for the file");
		}

		return new Result(true, "got video link and ffprobe ok", vf);
	}

	public boolean shareFile(String userMail, String id) {
		try {
			List<FileMemberActionResult> share = client.sharing()
					.addFileMemberBuilder(id, Arrays.asList(MemberSelector.email(userMail)))
					.withQuiet(true).withAccessLevel(AccessLevel.VIEWER).start();
			if (!share.isEmpty()) {
				return share.get(0).getResult().isSuccess();
			}
		} catch (DbxException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return false;
	}

	public void saveThumbnail(String id, String path, String size) {
		log.info("got thumb size: " + size);
		String outPath = "/var/www/html/thumbs/" + id.replaceAll("id:", "") + "-" + size
				+ ".jpg";
		if (new File(outPath).exists()) {
			return;
		}

		ThumbnailSize dropboxThumbSize = ThumbnailSize.W32H32;
		if (size.equalsIgnoreCase("med")) {
			dropboxThumbSize = ThumbnailSize.W128H128;
		} else if (size.equalsIgnoreCase("hi")) {
			dropboxThumbSize = ThumbnailSize.W640H480;
		}

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			DbxDownloader<FileMetadata> dl = client.files().getThumbnailBuilder(path)
					.withFormat(ThumbnailFormat.JPEG).withSize(dropboxThumbSize).start();
			dl.download(outputStream);
			try (OutputStream file = new FileOutputStream(outPath)) {
				outputStream.writeTo(file);
			}
			dl.close();
		} catch (DbxException | IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public List<Video> listVideos(String thumbSize) {
		List<Video> videos = new ArrayList<>();
		try {
			FullAccount account = client.users().getCurrentAccount();
			log.info(account.getName().getDisplayName());

			ListFolderResult result = client.files().listFolderBuilder("/filmy")
					.withIncludeDeleted(false).withIncludeMediaInfo(true).withRecursive(false)
					.start();
			List<Metadata> folderContents = result.getEntries();

			List<CompletableFuture<Void>> thumbFutures = new ArrayList<>();

			folderContents.stream().forEach(file -> {
				if (file instanceof FileMetadata) {
					FileMetadata fmd = (FileMetadata) file;
					Video vf = new Video();
					vf.setModified(fmd.getClientModified());
					vf.setName(fmd.getName());
					vf.setExtension(FilenameUtils.getExtension(fmd.getName()));
					vf.setVideoId(fmd.getId());
					vf.setSize(fmd.getSize());
					vf.setPath(fmd.getPathLower());

					MediaMetadata media = fmd.getMediaInfo().getMetadataValue();
					vf.setWidth((int) media.getDimensions().getWidth());
					vf.setHeight((int) media.getDimensions().getHeight());
					vf.setTimeTaken(media.getTimeTaken());

					if (media instanceof VideoMetadata) {
						VideoMetadata video = (VideoMetadata) media;
						vf.setDuration(video.getDuration());
						if (video.getLocation() != null) {
							vf.setGpsLat(video.getLocation().getLatitude());
							vf.setGpsLong(video.getLocation().getLongitude());
						}

						thumbFutures.add(CompletableFuture.runAsync(() -> this
								.saveThumbnail(vf.getVideoId(), vf.getPath(), thumbSize)));

						vf.setThumbnailLink("http://sdi.myftp.org:81/thumbs/"
								+ vf.getVideoId().replaceAll("id:", "") + "-" + thumbSize
								+ ".jpg");

						videos.add(vf);
					}
				}
			});

			CompletableFuture<Void>[] f = thumbFutures
					.toArray(new CompletableFuture[thumbFutures.size()]);
			CompletableFuture.allOf(f).join();

		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			return videos;
		}

		return videos;
	}
}
