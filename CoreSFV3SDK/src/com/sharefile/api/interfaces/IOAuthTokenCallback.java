package com.sharefile.api.interfaces;

import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.exceptions.SFSDKException;

public interface IOAuthTokenCallback
{
    public void onSuccess(SFOAuth2Token token);
    public void onError(SFSDKException exception);
}
