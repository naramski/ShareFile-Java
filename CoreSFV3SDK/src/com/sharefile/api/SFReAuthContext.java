package com.sharefile.api;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFSDKException;
import com.sharefile.api.interfaces.ISFApiClient;
import com.sharefile.api.interfaces.ISFApiResultCallback;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.interfaces.ISFReAuthHandler;
import com.sharefile.api.interfaces.ISFReExecuteQuery;
import com.sharefile.api.utils.Utils;

/**
 *   This class should receive all the information to re-execute the original query that caused the auth exception.
 */
public final class SFReAuthContext<T>
{	
	private final ISFQuery<T> mQuery;
	private final ISFApiResultCallback<T> mOriginalListener;
	private final AtomicBoolean mIsCancelled = new AtomicBoolean(false);
	private final ISFApiClient mSFApiClient;
	private final ISFReAuthHandler mReauthHandler;

    public ISFQuery<T> getQuery()
    {
        return mQuery;
    }

    public ISFApiClient getApiClient()
    {
        return mSFApiClient;
    }

    public ISFApiResultCallback getCallback()
    {
        return mOriginalListener;
    }

    public ISFReAuthHandler getReAuthHandler()
    {
        return mReauthHandler;
    }
		
	@SFSDKDefaultAccessScope
	SFReAuthContext(ISFQuery<T> sfapiApiqueri,ISFApiResultCallback<T> originalListener, ISFReAuthHandler reauthHandler,ISFApiClient sfApiClient)
	{
		mQuery = sfapiApiqueri;
		mOriginalListener = originalListener;
		mSFApiClient = sfApiClient;
		mReauthHandler = reauthHandler;
	}
			
	public void reExecuteQueryWithCredentials(String userName, String password, ISFReExecuteQuery<T> reExecutor) throws SFInvalidStateException
	{		
		if(mIsCancelled.get())
		{
			throw new SFInvalidStateException("Re-Authcontext cancelled previously");
		}
		
		mQuery.setCredentials(userName, password);
				
		reExecutor.execute(mSFApiClient, mQuery, mOriginalListener, mReauthHandler);		
	}
	
	/**
	 *  Actually nothing needs to be cancelled since there are no threads waiting on this object. 
	 *  We just return a convenience call to return the Query which triggered the auth problem
	 *  and invalidate the ReAuthContext so that no-one can call the proceedWithCredentials() after cancel has been called.
	 */	
	public ISFQuery<T> cancel()
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

    public void callErrorListener(SFSDKException exception)
    {
        Utils.safeCallErrorListener(mOriginalListener,exception,mQuery);
    }
}