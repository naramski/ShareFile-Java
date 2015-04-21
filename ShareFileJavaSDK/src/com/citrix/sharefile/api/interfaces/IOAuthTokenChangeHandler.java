package com.citrix.sharefile.api.interfaces;

import com.citrix.sharefile.api.authentication.SFOAuth2Token;
import com.citrix.sharefile.api.exceptions.SFSDKException;

/*
 * These callbacks are called when you set a oAuth token change handler from your application
 * so that the ISFApiClient can attempt to auto-renew the auth tokens on behalf of your application
 * and convey the details so that your app can handle the errors appropriately.
 */
public interface IOAuthTokenChangeHandler
{
	public void storeNewToken(ISFApiClient apiClient, SFOAuth2Token newAccessToken);
    public void tokenRenewFailed(ISFApiClient apiClient, SFSDKException exception);
}