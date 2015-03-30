package com.sharefile.api.interfaces;

import com.sharefile.api.SFV3Error;
import com.sharefile.api.models.SFODataObject;

public interface ISFApiResultCallback<T>
{
	public void onSuccess(T object);
	public void onError(SFV3Error error, ISFQuery<T> mQuery);
}