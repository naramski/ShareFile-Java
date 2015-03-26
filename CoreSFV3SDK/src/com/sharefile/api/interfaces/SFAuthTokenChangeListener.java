package com.sharefile.api.interfaces;

import com.sharefile.api.SFV3Error;
import com.sharefile.api.authentication.SFOAuth2Token;

public interface SFAuthTokenChangeListener 
{
	public void storeNewToken(SFOAuth2Token newAccessToken);
    public void tokenRenewFailed(SFV3Error error);
}