package com.sharefile.api.interfaces;

import com.sharefile.api.SFQueryStream;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFNotAuthorizedException;
import com.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.sharefile.api.exceptions.SFOtherException;
import com.sharefile.api.exceptions.SFServerException;
import com.sharefile.api.models.SFODataObject;

import java.io.InputStream;

public interface ISFApiClient extends IOAuthTokenChangeHandler
{
    public <T extends SFODataObject> T executeQuery(ISFQuery<T> query)
            throws SFServerException, SFInvalidStateException,
            SFNotAuthorizedException, SFOAuthTokenRenewException,SFOtherException;

    public InputStream executeQuery(SFQueryStream query)
            throws SFServerException, SFInvalidStateException,
            SFNotAuthorizedException, SFOAuthTokenRenewException,SFOtherException;

    public String getUserId();

    public <T> ISFApiExecuteQuery getExecutor(ISFQuery<T> query,
                                   ISFApiResultCallback<T> apiResultCallback,
                                   ISFReAuthHandler reAuthHandler)
            throws SFInvalidStateException;
}