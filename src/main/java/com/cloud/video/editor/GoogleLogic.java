package com.cloud.video.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.oauth2.client.OAuth2ClientContext;

import com.cloud.video.editor.model.Result;
import com.cloud.video.editor.model.Video;
import com.cloud.video.editor.utils.SysUtils;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.File.VideoMediaMetadata;
import com.google.api.services.drive.model.FileList;

import lombok.extern.java.Log;

@Log
public class GoogleLogic implements CloudVideoSource {

	private static HttpTransport httpTransport;

	private static final String APPLICATION_NAME = "VideoManager";

	private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private OAuth2ClientContext oauth;

	static {
		try {
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			System.exit(1);
		}
	}

	public GoogleLogic(OAuth2ClientContext oauth) {
		this.oauth = oauth;
	}

	private Credential authorize() {
		Credential credential = new GoogleCredential.Builder().setTransport(httpTransport)
				.setJsonFactory(JSON_FACTORY).build();
		credential.setAccessToken(oauth.getAccessToken().getValue());
		return credential;
	}

	private Drive getDriveService() {
		Credential credential = authorize();
		return new Drive.Builder(httpTransport, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME).build();
	}

	@Override
	public Result getDirectLink(Video vf) {
		Stream<String> out = SysUtils.getResultStream(
				"/bin/bash /home/jp/sbin/get_drive_url " + vf.getWebContentLink());
		Optional<String> url = out.filter(line -> line.contains("googleusercontent"))
				.findFirst();
		if (url.isPresent()) {
			vf.setDirectContentLink(url.get());
			return new Result(true, "link fetched ok");
		}
		return new Result(false, "link fetch failed");
	}

	@Override
	public List<Video> getVideos(String thumbSize) {
		try {
			Drive service = getDriveService();
			FileList result = service.files().list().setQ("mimeType='video/mp4'")
					.setSpaces("drive").setPageSize(10)
					.setFields("nextPageToken, files(id, name, thumbnailLink, webContentLink, "
							+ "webViewLink, videoMediaMetadata, shared, "
							+ "size, fileExtension, createdTime, modifiedTime)")
					.execute();
			List<File> files = result.getFiles();
			return files.stream().map(file -> {
				Video gf = new Video();
				gf.setName(file.getName());
				gf.setVideoId(file.getId());
				if (file.getThumbnailLink() != null) {
					gf.setThumbnailLink(file.getThumbnailLink());
				}
				VideoMediaMetadata meta = file.getVideoMediaMetadata();
				if (meta != null) {
					gf.setWidth(meta.getWidth());
					gf.setHeight(meta.getHeight());
					gf.setDuration(meta.getDurationMillis());
				}
				gf.setWebContentLink(file.getWebContentLink());
				gf.setCreated(new Date(file.getCreatedTime().getValue()));
				gf.setModified(new Date(file.getModifiedTime().getValue()));
				gf.setExtension(file.getFileExtension());
				gf.setSize(file.getSize());

				return gf;
			}).collect(Collectors.toList());
		} catch (IOException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		return new ArrayList<>();
	}
}
