package com.citrix.sharefile.api.exceptions;


@SuppressWarnings("serial")
public class SFServerException extends SFSDKException
{
    private final int httpErrorCode;

	public SFServerException(int httpErrorCode, String detailedMessage)
	{
		super(detailedMessage);
        this.httpErrorCode = httpErrorCode;
	}

    public int getHttpResponseCode()
    {
        return httpErrorCode;
    }
}