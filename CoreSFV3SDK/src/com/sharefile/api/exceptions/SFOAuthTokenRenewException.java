package com.sharefile.api.exceptions;

import com.sharefile.api.SFV3Error;

import javax.net.ssl.HttpsURLConnection;

@SuppressWarnings("serial")
public class SFOAuthTokenRenewException extends SFSDKException
{
	public SFOAuthTokenRenewException(String reason)
	{
		super(new RuntimeException(reason));
	}

    public SFOAuthTokenRenewException(Throwable e)
    {
        super(e);
    }
}