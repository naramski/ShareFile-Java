package com.sharefile.api.interfaces;

import com.sharefile.api.SFApiQuery;
import com.sharefile.api.V3Error;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.models.SFODataObject;

public interface SFApiResponseListener<T extends SFODataObject>
{
	public void sfapiSuccess(T object);
	public void sfApiError(V3Error error, SFApiQuery<T> sfapiApiqueri);
	public void sfApiStoreNewToken(SFOAuth2Token newAccessToken, SFApiQuery<T> sfapiApiqueri);
}