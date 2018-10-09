package com.cloud.video.editor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

/**
 * Stores user's token infos (session, cursor) in redis.
 */
@Repository
public class UserTokenRepository {

    private final Map<String, String> dropboxTokens = new HashMap<String, String>();

    public UserTokenRepository() {
    }

    public void setValue(final String key, final String value) {
    	dropboxTokens.put(key, value);
    }

    public String getValue(final String key) {
        return dropboxTokens.get(key);
    }
}