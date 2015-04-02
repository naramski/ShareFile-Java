package com.sharefile.api.interfaces;

import com.sharefile.api.exceptions.SFSDKException;

public interface ISFApiResultCallback<T>
{
	public void onSuccess(T object);
	public void onError(SFSDKException exception, ISFQuery<T> mQuery);
}