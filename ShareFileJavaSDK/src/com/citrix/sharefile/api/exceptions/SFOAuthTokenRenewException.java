package com.citrix.sharefile.api.exceptions;



import javax.net.ssl.HttpsURLConnection;

@SuppressWarnings("serial")
public class SFOAuthTokenRenewException extends SFSDKException
{
	public SFOAuthTokenRenewException(String reason)
	{
		super(reason);
	}

    public SFOAuthTokenRenewException(Throwable e)
    {
        super(e);
    }
}