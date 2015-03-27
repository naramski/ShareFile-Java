package com.sharefile.api.interfaces;

import com.sharefile.api.SFV3Error;
import com.sharefile.api.interfaces.ISFApiCallback;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.utils.Utils;

/**
 *  Helper class for the app to implement internal listeners
 * @param <T>
 * @param <T>
 */
public abstract class ISFApiListenerEx<T extends SFODataObject> implements ISFApiCallback<T>
{
	private ISFApiCallback<T> mListener;
	
	public ISFApiListenerEx(ISFApiCallback<T> listener)
	{
		mListener = listener;
	}
	
	public ISFApiListenerEx()
	{		
	}
	
	public void setListener(ISFApiCallback<T> listener)
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
	public synchronized final Thread executeQuery(SFApiClient apiClient , SFApiQuery<T> query , ISFApiCallback<T> listener, ISFReAuthHandler reauthHandler) throws SFInvalidStateException
	{
		return apiClient.executeQuery(query, listener, reauthHandler);
	}*/
}