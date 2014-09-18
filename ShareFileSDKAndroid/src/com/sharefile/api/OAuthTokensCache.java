package com.sharefile.api;

import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.java.log.SLog;

import java.util.Hashtable;

public class OAuthTokensCache {
    public static final String TAG = "OauthTokensCache";

    public static OAuthTokensCache instance = null;
    public static synchronized OAuthTokensCache getInstance() {
        if ( instance==null ) instance = new OAuthTokensCache();
        return instance;
    }

    private Hashtable<String, SFOAuth2Token> tokens = new Hashtable<String, SFOAuth2Token>();
    public SFOAuth2Token get(String userId) {
        SLog.v(TAG, "Get access token for: " + userId);
        return tokens.get(userId);
    }

    public void set(String userId, SFOAuth2Token token) {
        if ( userId==null || userId.isEmpty() || token==null ) {
            // ...
            return;
        }

        SLog.d(TAG, "Set access token for: " + userId);
        tokens.put(userId, token);
    }
}
