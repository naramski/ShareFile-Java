package com.sharefile.api.interfaces;

import com.sharefile.api.SFApiQuery;
import com.sharefile.api.models.SFODataObject;

public interface SFApiResponseListener<T extends SFODataObject>
{
	public void sfapiSuccess(T object);
	public void sfApiError(int errorCode,String errorMessage, SFApiQuery<T> asApiqueri);
}