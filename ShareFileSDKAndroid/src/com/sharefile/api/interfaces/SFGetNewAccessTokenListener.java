package com.sharefile.api.interfaces;

import com.sharefile.api.V3Error;
import com.sharefile.api.authentication.SFOAuth2Token;

public interface SFGetNewAccessTokenListener 
{
	public void successGetAccessToken(SFOAuth2Token accessToken);
	public void errorGetAccessToken(V3Error v3error);
}
