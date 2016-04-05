package com.citrix.sharefile.api;

import com.citrix.sharefile.api.exceptions.SFInvalidStateException;
import com.citrix.sharefile.api.exceptions.SFNotAuthorizedException;
import com.citrix.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.citrix.sharefile.api.exceptions.SFOtherException;
import com.citrix.sharefile.api.exceptions.SFServerException;
import com.citrix.sharefile.api.interfaces.ISFApiClient;
import com.citrix.sharefile.api.models.SFODataObject;

public class SFApiQuery<T extends SFODataObject> extends AbstractSFApiQuery<T>
{
    public SFApiQuery(ISFApiClient client)
    {
		super(client);
    }

	@Override
	public T execute() throws SFInvalidStateException, SFServerException,
			SFNotAuthorizedException, SFOAuthTokenRenewException, SFOtherException
	{

		if(apiClient==null)
		{
			throw new SFInvalidStateException("No valid client object set for query");
		}

		return (T)apiClient.executeQuery(this);
	}

}