package com.sharefile.api.interfaces;

import com.sharefile.api.SFV3Error;
import com.sharefile.api.models.SFODataObject;

import java.io.InputStream;

public interface SFApiStreamResponse
{
	public void sfApiSuccess(InputStream object);
	public void sfApiError(SFV3Error error, ISFQuery<InputStream> mQuery);
}