package com.cloud.video.editor;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.StringUtils.isEmpty;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxSessionStore;
import com.dropbox.core.DbxStandardSessionStore;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;

@Service
public class DropboxService {

	private static final Logger LOG = getLogger(DropboxService.class);

	private final DropboxConfigProperties dropboxConfigProp;

	private final DbxWebAuth auth;

	private final DbxRequestConfig requestConfig;

	private final UserTokenRepository userTokenRepository;

	public DropboxService(final DropboxConfigProperties dropboxConfigProperties, final DbxWebAuth auth,
			final DbxRequestConfig requestConfig, final UserTokenRepository userTokenRepository) {
		this.dropboxConfigProp = dropboxConfigProperties;
		this.auth = auth;
		this.requestConfig = requestConfig;
		this.userTokenRepository = userTokenRepository;

	}

	public DbxClientV2 getClient(final String userId) throws Exception {
		String accessToken = getTokenAndCheckIsTokenExists(userId);
		return new DbxClientV2(requestConfig, accessToken);
	}

	public String startAuth(final HttpSession session) {
		final DbxSessionStore csrfTokenStore = new DbxStandardSessionStore(session,
				dropboxConfigProp.getSessionStore().getKey());
		final DbxWebAuth.Request authRequest = DbxWebAuth.newRequestBuilder()
				.withRedirectUri(dropboxConfigProp.getRedirectUri(), csrfTokenStore).build();
		return auth.authorize(authRequest);
	}

	public void finishAuthAndSaveUserDetails(final HttpSession session, final Map<String, String[]> parameterMap)
			throws Exception {
		final DbxSessionStore csrfTokenStore = new DbxStandardSessionStore(session,
				dropboxConfigProp.getSessionStore().getKey());
		final DbxAuthFinish authFinish = auth.finishFromRedirect(dropboxConfigProp.getRedirectUri(), csrfTokenStore,
				parameterMap);
		final String accessToken = authFinish.getAccessToken();
		final DbxClientV2 client = new DbxClientV2(requestConfig, accessToken);
		final String userId = authFinish.getUserId();
		final ListFolderResult listFolderResult = client.files().listFolderBuilder("").withRecursive(true).start();
		saveAccessTokenAndActualCursor(userId, accessToken, listFolderResult);
	}

	private void saveAccessTokenAndActualCursor(final String userId, final String accessToken,
			final ListFolderResult listFolderResult) {
		userTokenRepository.setValue(userId, accessToken);
	}

	private String getTokenAndCheckIsTokenExists(final String userId) throws IllegalAccessException {
		final String token = userTokenRepository.getValue(userId);
		if (isEmpty(token)) {
			throw new IllegalAccessException("Please allow access first.");
		}
		return token;
	}

}