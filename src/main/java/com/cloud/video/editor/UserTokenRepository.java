package com.cloud.video.editor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import lombok.NoArgsConstructor;

/**
 * Stores user's token infos (session, cursor) in redis.
 */
@Repository
@NoArgsConstructor
public class UserTokenRepository {

    private final Map<String, String> dropboxTokens = new HashMap<String, String>();

    public void setValue(final String key, final String value) {
    	dropboxTokens.put(key, value);
    }

    public String getValue(final String key) {
        return dropboxTokens.get(key);
    }
}