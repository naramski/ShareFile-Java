package com.sharefile.api;

import com.sharefile.api.interfaces.IOAuthTokenChangeHandler;
import com.sharefile.api.interfaces.ISFReAuthHandler;

public class SFSdk
{
    private static String mClientId;
    private static String mClientSecret;
    private static String mRedirectUrl;
    private static ISFReAuthHandler mReAuthHandler;
    private static IOAuthTokenChangeHandler mOAuthTokenChangeHandler;

    public static void init(String clientId, String clientSecret, String redirectUrl)
    {
        mClientId = clientId;
        mClientSecret = clientSecret;
        mRedirectUrl = redirectUrl;
    }

    public static void init(String clientId, String clientSecret, String redirectUrl,
                            ISFReAuthHandler reAuthHandler,
                            IOAuthTokenChangeHandler authTokenChangeHandler)
    {
        mClientId = clientId;
        mClientSecret = clientSecret;
        mRedirectUrl = redirectUrl;
        mReAuthHandler = reAuthHandler;
        mOAuthTokenChangeHandler = authTokenChangeHandler;
    }

    public static String getClientId()
    {
        return mClientId;
    }

    public static String getClientSecret()
    {
        return mClientSecret;
    }

    public static String getRedirectUrl()
    {
        return mRedirectUrl;
    }

    public static ISFReAuthHandler getReAuthHandler()
    {
        return mReAuthHandler;
    }

    public static IOAuthTokenChangeHandler getOAuthTokenChangeHandler()
    {
        return mOAuthTokenChangeHandler;
    }
}