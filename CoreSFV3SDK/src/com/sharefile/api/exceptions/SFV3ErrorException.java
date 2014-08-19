package com.sharefile.api.exceptions;

import javax.net.ssl.HttpsURLConnection;

import com.sharefile.api.SFV3Error;

@SuppressWarnings("serial")
public class SFV3ErrorException extends SFAbstractException
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
		
		if(mV3Error!=null && mV3Error.getServerResponse().httpResponseCode ==  HttpsURLConnection.HTTP_UNAUTHORIZED)
		{
			result = true;
		}
		
		return result;
	}
}