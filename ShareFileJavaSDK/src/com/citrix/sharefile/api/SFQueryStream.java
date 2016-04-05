package com.citrix.sharefile.api;

import com.citrix.sharefile.api.exceptions.SFInvalidStateException;
import com.citrix.sharefile.api.exceptions.SFNotAuthorizedException;
import com.citrix.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.citrix.sharefile.api.exceptions.SFOtherException;
import com.citrix.sharefile.api.exceptions.SFServerException;
import com.citrix.sharefile.api.interfaces.ISFApiClient;

import java.io.InputStream;

public class SFQueryStream extends AbstractSFApiQuery<InputStream>
{
    public SFQueryStream(ISFApiClient client)
    {
        super(client);
    }

    @Override
    public InputStream execute() throws SFInvalidStateException, SFServerException, SFNotAuthorizedException, SFOAuthTokenRenewException, SFOtherException {

        if(apiClient==null)
        {
            throw new SFInvalidStateException("No valid client object set for query");
        }

        return apiClient.executeQueryEx(this);
    }
}