package com.sharefile.api.interfaces;

import com.sharefile.api.SFQueryStream;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFNotAuthorizedException;
import com.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.sharefile.api.exceptions.SFV3ErrorException;
import com.sharefile.api.models.SFODataObject;

import java.io.InputStream;

public interface ISFApiClient extends IOAuthTokenChangeHandler
{
    public <T extends SFODataObject> T executeQuery(ISFQuery<T> query)
            throws SFV3ErrorException, SFInvalidStateException, SFNotAuthorizedException, SFOAuthTokenRenewException;

    public InputStream executeQuery(SFQueryStream query)
            throws SFV3ErrorException, SFInvalidStateException, SFNotAuthorizedException, SFOAuthTokenRenewException;

    public String getUserId();
}