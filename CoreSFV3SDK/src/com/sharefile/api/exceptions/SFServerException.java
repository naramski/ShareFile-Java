package com.sharefile.api.exceptions;


@SuppressWarnings("serial")
public class SFServerException extends SFSDKException
{
	public SFServerException(String detailedMessage)
	{
		super(detailedMessage);
	}
}