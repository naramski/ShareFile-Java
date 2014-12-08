package com.sharefile.api;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.interfaces.ISFReAuthHandler;
import com.sharefile.api.interfaces.ISFReExcecuteQuery;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.utils.Utils;

/**
 *   This class should receive all the information to re-execute the original query that caused the auth exception.
 */
public final class SFReAuthContext<T extends SFODataObject> 
{	
	private final ISFQuery<T> mQuery;
	private final SFApiResponseListener<T> mOriginalListener;	
	private final AtomicBoolean mIsCancelled = new AtomicBoolean(false);
	private final SFApiClient mSFApiClient;
	private final ISFReAuthHandler mReauthHandler;
		
	@SFSDKDefaultAccessScope
	SFReAuthContext(ISFQuery<T> sfapiApiqueri,SFApiResponseListener<T> originalListener, ISFReAuthHandler reauthHandler,SFApiClient sfApiClient)	
	{
		mQuery = sfapiApiqueri;
		mOriginalListener = originalListener;
		mSFApiClient = sfApiClient;
		mReauthHandler = reauthHandler;
	}
			
	public void reExecuteQueryWithCredentials(String userName, String password, ISFReExcecuteQuery<T> reExecutor) throws SFInvalidStateException 
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

    public void callErrorListener(SFV3Error sfv3Error)
    {
        Utils.safeCallErrorListener(mOriginalListener,sfv3Error,mQuery);
    }
}