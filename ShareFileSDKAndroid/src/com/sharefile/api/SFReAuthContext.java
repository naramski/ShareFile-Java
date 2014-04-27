package com.sharefile.api;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sharefile.api.exceptions.SFInvalidStateException;

/**
 *   This class should receive all the information to re-execute the original query that caused the auth exception.
 */
public final class SFReAuthContext
{
	@SuppressWarnings("rawtypes")
	private final SFApiQuery mQuery;
	private final SFApiListenerReauthHandler mOriginalListener;	
	private AtomicBoolean mIsCancelled = new AtomicBoolean(false);
	private final SFApiClient mSFApiClient;
	
	@SuppressWarnings("rawtypes")
	SFReAuthContext(SFApiQuery query,SFApiListenerReauthHandler originalListener,SFApiClient sfApiClient)	
	{
		mQuery = query;
		mOriginalListener = originalListener;
		mSFApiClient = sfApiClient;
	}
		
	@SuppressWarnings("unchecked")
	public void proceedWithCredentials(String userName, String password) throws SFInvalidStateException 
	{		
		if(mIsCancelled.get())
		{
			throw new SFInvalidStateException("Re-Authcontext cancelled previously");
		}
		
		mQuery.setCredentials(userName, password);
		mSFApiClient.executeQueryInternal(mQuery,mOriginalListener,false);								
	}
	
	/**
	 *  Actually nothing needs to be cancelled since there are no threads waiting on this object. 
	 *  We just return a convenience call to return the Query which triggered the auth problem
	 *  and invalidate the ReAuthContext so that no-one can call the proceedWithCredentials() after cancel has been called.
	 */
	@SuppressWarnings("rawtypes")
	public SFApiQuery cancel()
	{
		mIsCancelled.set(true);
		return mQuery;
	}
	
	public final String getUserId()
	{
		return mSFApiClient.getUserId();
	}
	
	public final URI getQueryURL()
	{
		return mQuery.getLink();
	}
}