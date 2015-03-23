package com.sharefile.api.interfaces;

import com.sharefile.api.SFV3Error;
import com.sharefile.api.models.SFODataObject;

public interface SFApiResponseListener<T>
{
	public void sfApiSuccess(T object);
	public void sfApiError(SFV3Error error, ISFQuery<T> mQuery);	
}