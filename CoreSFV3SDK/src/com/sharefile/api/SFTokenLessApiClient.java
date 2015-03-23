package com.sharefile.api;

import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.interfaces.ISFReAuthHandler;
import com.sharefile.api.interfaces.SFAuthTokenChangeListener;


import java.net.URI;
import java.net.URISyntaxException;

public class SFTokenLessApiClient extends SFApiClient
{
    private static final String DUMMY_TOKEN_VALUE = "[TokenLessClient]";
    private static final String DUMMY_TOKEN_USER_ID = "TokenLessUserId";
    private static final String DUMMY_TOKEN_CLIENT_ID = "TokenLessClientId";
    private static final String DUMMY_TOKEN_CLIENT_SECRET = "TokenLessClientSecret";

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
}
