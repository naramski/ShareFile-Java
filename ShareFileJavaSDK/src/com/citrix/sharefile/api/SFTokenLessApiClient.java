package com.citrix.sharefile.api;

import com.citrix.sharefile.api.authentication.SFOAuth2Token;
import com.citrix.sharefile.api.exceptions.SFInvalidStateException;
import com.citrix.sharefile.api.interfaces.ISFReAuthHandler;

public class SFTokenLessApiClient extends SFApiClient
{
    private static final String DUMMY_TOKEN_VALUE = "[TokenLessClient]";
    private static final String DUMMY_TOKEN_USER_ID = "TokenLessUserId";
    private static final String DUMMY_TOKEN_CLIENT_ID = "TokenLessClientId";
    private static final String DUMMY_TOKEN_CLIENT_SECRET = "TokenLessClientSecret";

    /*
     * Note : a Token less ApiClient can be used for Standalone connectors so it may have a
     * ReAuthHandler!
     */
    public SFTokenLessApiClient(String subDomain,String apiControlPlane, ISFReAuthHandler reauthHandler) throws SFInvalidStateException
    {
        super(new SFOAuth2Token(DUMMY_TOKEN_VALUE,
                DUMMY_TOKEN_VALUE,
                DUMMY_TOKEN_VALUE,
                apiControlPlane,
                apiControlPlane,
                subDomain,
                0)
                , DUMMY_TOKEN_USER_ID, DUMMY_TOKEN_CLIENT_ID, DUMMY_TOKEN_CLIENT_SECRET, null, reauthHandler);
    }

    public SFTokenLessApiClient(String subDomain,String apiControlPlane) throws SFInvalidStateException
    {
        this(subDomain,apiControlPlane,null);
    }
}