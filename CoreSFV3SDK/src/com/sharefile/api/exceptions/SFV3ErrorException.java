package com.sharefile.api.exceptions;

import com.sharefile.api.SFV3Error;

import javax.net.ssl.HttpsURLConnection;

@SuppressWarnings("serial")
public class SFV3ErrorException extends SFSDKException
{
	private final SFV3Error mV3Error;
	
	public SFV3ErrorException(SFV3Error v3error) 
	{
		super(new RuntimeException());
		mV3Error = v3error;
	}
	
	public SFV3Error getV3Error()
	{
		return mV3Error;
	}
	
	public boolean isAuthException()
	{
		boolean result = false;
		
		if(mV3Error!=null && mV3Error.getHttpResponseCode() ==  HttpsURLConnection.HTTP_UNAUTHORIZED)
		{
			result = true;
		}
		
		return result;
	}

    @Override
    public String getMessage()
    {
        return mV3Error.errorDisplayString("");
    }

    @Override
    public String getLocalizedMessage()
    {
        return getMessage();
    }
}