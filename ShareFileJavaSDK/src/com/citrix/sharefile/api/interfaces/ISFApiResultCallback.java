package com.citrix.sharefile.api.interfaces;

import com.citrix.sharefile.api.exceptions.SFSDKException;

public interface ISFApiResultCallback<T>
{
	public void onSuccess(T object);
	public void onError(SFSDKException exception, ISFQuery<T> mQuery);
}