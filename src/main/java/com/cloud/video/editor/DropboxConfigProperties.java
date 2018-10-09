package com.cloud.video.editor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("dropbox.app")
public class DropboxConfigProperties {

    private final SessionStore sessionStore = new SessionStore();

    private String key = "50ebpclezbrd3dm";

    private String secret = "7ghvhw2fspv0ltr";

    private String redirectUri = "http://localhost:81/dropbox/finish-auth";

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(final String secret) {
        this.secret = secret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(final String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public SessionStore getSessionStore() {
        return sessionStore;
    }

    public static class SessionStore {

        private String key;

        public String getKey() {
            return key;
        }

        public void setKey(final String key) {
            this.key = key;
        }

    }

}