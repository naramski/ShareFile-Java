package com.sharefile.api.interfaces;

import com.sharefile.api.authentication.SFOAuth2Token;

public interface IOAuthTokenChangeListener
{
	public void storeNewToken(SFOAuth2Token newAccessToken);
}