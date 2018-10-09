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

public class DropboxLogic {

	DbxClientV2 client;

	public DropboxLogic(String accessToken) {
		DbxRequestConfig config = DbxRequestConfig.newBuilder("video-manager/0.1").build();
		client = new DbxClientV2(config, accessToken);
	}

	public void getDirectLink(Video vf) {
		GetTemporaryLinkResult res;
		try {
			res = client.files().getTemporaryLink(vf.getPath());
			String path = res.getLink();
			vf.setDirectContentLink(path);
			Result probeRes = ProbeUtils.probeVideo(path);
			if (!probeRes.isSuccess()) {
				return;
			}

			Probe p = (Probe) probeRes.getResult();
			Optional<Stream> videoStreamOpt = p.getStreams().stream()
					.filter(s -> s.getCodec_type().equalsIgnoreCase("video")).findFirst();
			if (!videoStreamOpt.isPresent()) {
				return;
			}
			Stream video = videoStreamOpt.get();
			String fpsString = video.getAvg_frame_rate();
			if (fpsString != null) {
				int fpsNum = Integer.parseInt(fpsString.split("\\/")[0]);
				int fpsDenum = Integer.parseInt(fpsString.split("\\/")[1]);
				double fps = Double.valueOf(1.0 * fpsNum / fpsDenum);
				System.out.println("fps: " + fps);
				vf.setFps(fps);
			}

			vf.setFrameCount(video.getNb_frames());

		} catch (DbxException e) {
		}
	}

	public boolean shareFile(String userMail, String id) {
		try {
			List<FileMemberActionResult> share = client.sharing()
					.addFileMemberBuilder(id, Arrays.asList(MemberSelector.email(userMail))).withQuiet(true)
					.withAccessLevel(AccessLevel.VIEWER).start();
			if (!share.isEmpty()) {
				return share.get(0).getResult().isSuccess();
			}
		} catch (DbxException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void saveThumbnail(String id, String path, String size) {
		System.out.println("got thumb size: " + size);
		String outPath = "/var/www/html/thumbs/" + id.replaceAll("id:", "") + "-" + size + ".jpg";
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
			DbxDownloader<FileMetadata> dl = client.files().getThumbnailBuilder(path).withFormat(ThumbnailFormat.JPEG)
					.withSize(dropboxThumbSize).start();
			dl.download(outputStream);
			try (OutputStream file = new FileOutputStream(outPath)) {
				outputStream.writeTo(file);
			}
		} catch (DbxException | IOException e) {
			e.printStackTrace();
		}
	}

	public List<Video> listVideos(String thumbSize) {
		List<Video> videos = new ArrayList<Video>();
		try {
			FullAccount account = client.users().getCurrentAccount();
			System.out.println(account.getName().getDisplayName());

			ListFolderResult result = client.files().listFolderBuilder("/filmy").withIncludeDeleted(false)
					.withIncludeMediaInfo(true).withRecursive(false).start();
			List<Metadata> folderContents = result.getEntries();

			List<CompletableFuture> thumbFutures = new ArrayList<CompletableFuture>();

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
					vf.setWidtn((int) media.getDimensions().getWidth());
					vf.setHeight((int) media.getDimensions().getHeight());
					vf.setTimeTaken(media.getTimeTaken());

					if (media instanceof VideoMetadata) {
						VideoMetadata video = (VideoMetadata) media;
						vf.setDuration(video.getDuration());
						if (video.getLocation() != null) {
							vf.setGpsLat(video.getLocation().getLatitude());
							vf.setGpsLong(video.getLocation().getLongitude());
						}

						thumbFutures.add(CompletableFuture.runAsync(() -> {
							this.saveThumbnail(vf.getVideoId(), vf.getPath(), thumbSize);
						}));

						vf.setThumbnailLink("http://sdi.myftp.org:81/thumbs/" + vf.getVideoId().replaceAll("id:", "")
								+ "-" + thumbSize + ".jpg");

						videos.add(vf);
					}
				}
			});

			CompletableFuture[] f = thumbFutures.toArray(new CompletableFuture[thumbFutures.size()]);
			CompletableFuture.allOf(f).join();

		} catch (Exception e) {
			e.printStackTrace();
			return videos;
		}

		return videos;
	}
}
