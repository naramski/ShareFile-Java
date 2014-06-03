package com.sharefile.api.interfaces;

import com.sharefile.api.authentication.SFOAuth2Token;

public interface SFAuthTokenChangeListener 
{
	public void sfApiStoreNewToken(SFOAuth2Token newAccessToken);
}
