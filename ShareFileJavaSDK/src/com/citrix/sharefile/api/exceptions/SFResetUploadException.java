package com.citrix.sharefile.api.exceptions;


@SuppressWarnings("serial")
public class SFResetUploadException extends SFServerException
{
	public SFResetUploadException(SFServerException e)
	{
		super(e.getHttpResponseCode(),e.getLocalizedMessage());
	}
}