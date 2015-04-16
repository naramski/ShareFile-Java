package com.sharefile.api.interfaces;

import com.sharefile.api.authentication.SFOAuth2Token;

public interface IOAuthTokenChangeHandler
{
	public void storeNewToken(ISFApiClient apiClient, SFOAuth2Token newAccessToken);
}