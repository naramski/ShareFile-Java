package com.sharefile.api.interfaces;

import com.sharefile.api.SFV3Error;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.utils.Utils;

/**
 *  Helper class for the app to implement internal listeners
 * @param <T>
 * @param <T>
 */
public abstract class ISFApiResultListenerEx<T extends SFODataObject> implements ISFApiResultCallback<T>
{
	private ISFApiResultCallback<T> mListener;
	
	public ISFApiResultListenerEx(ISFApiResultCallback<T> listener)
	{
		mListener = listener;
	}
	
	public ISFApiResultListenerEx()
	{		
	}
	
	public void setListener(ISFApiResultCallback<T> listener)
	{
		mListener = listener;
	}
	
	@Override
	public void onError(SFV3Error error, ISFQuery<T> sfapiApiqueri)
	{		
		Utils.safeCallErrorListener(mListener, error, sfapiApiqueri);
	}
	
	@Override
	public void onSuccess(T object)
	{		
	  Utils.safeCallSuccess(mListener, object);	
	}
	
	/*
	public synchronized final Thread executeQuery(SFApiClient apiClient , SFApiQuery<T> query , ISFApiResultCallback<T> listener, ISFReAuthHandler reauthHandler) throws SFInvalidStateException
	{
		return apiClient.executeQuery(query, listener, reauthHandler);
	}*/
}