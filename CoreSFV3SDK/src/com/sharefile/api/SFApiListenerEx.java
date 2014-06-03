package com.sharefile.api;

import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.interfaces.ISFReAuthHandler;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.utils.Utils;

/**
 *  Helper class for the app to implement internal listeners
 * @param <T>
 * @param <T>
 */
public abstract class SFApiListenerEx<T extends SFODataObject> implements SFApiResponseListener<T> 
{
	private final SFApiResponseListener<T>  mListener;
	
	public SFApiListenerEx(SFApiResponseListener<T> listener) 
	{
		mListener = listener;
	}
		
	@Override
	public void sfApiError(SFV3Error error, SFApiQuery<T> sfapiApiqueri) 
	{		
		Utils.safeCallErrorListener(mListener, error, sfapiApiqueri);
	}
	
	@Override
	public void sfApiSuccess(T object) 
	{		
	  Utils.safeCallSuccess(mListener, object);	
	}
	
	public synchronized final Thread executeQuery(SFApiClient client , SFApiQuery<T> query , SFApiResponseListener<T> listener, ISFReAuthHandler reauthHandler) throws SFInvalidStateException
	{
		return client.executeQuery(query, listener, reauthHandler);
	}
}