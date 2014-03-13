package com.sharefile.api;

import java.lang.reflect.Type;

import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.entities.SFSessionsEntity;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.https.SFApiRunnable;
import com.sharefile.api.interfaces.SFApiClientInitListener;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSession;

public class SFApiClient 
{
	private static final String TAG = "-SFApiClient";
	
	public static final String MSG_INVALID_STATE_OAUTH_NULL = "Invalid state: Oauth token not initialized for SFApiClient";
	
	private SFOAuth2Token mOAuthToken = null;
	private SFSession mSession = null;
	private SFApiClientInitListener mClientInitListner = null;
	
	public SFApiClient(SFOAuth2Token outhToken) throws SFInvalidStateException
	{		
		mOAuthToken = outhToken;//TODO: Shouldn't this be cloned using a deep copy?	
		
		validateStateBeforeInit();
	}
		
	private SFApiResponseListener<SFSession> mListnererGetSession = new SFApiResponseListener<SFSession>() 
	{
		@Override
		public void sfapiSuccess(SFSession sfsession) 
		{			
			mSession = sfsession; //TODO: deep copy needed?
			SFLog.d2(TAG, "API SUCCESS. Session object = %s", sfsession.getName());
			
			//TODO: can we have generic pattern for callback calling
			if(mClientInitListner!=null)
			{
				mClientInitListner.sfApiClientInitSuccess();
			}
		}

		@Override
		public void sfApiError(int errorCode, String errorMessage,SFApiQuery<SFSession> asApiqueri) 
		{		
			SFLog.d2(TAG, "API FAILURE. error code = %d", errorCode);
			
			//TODO: can we have generic pattern for callback calling
			if(mClientInitListner!=null)
			{
				mClientInitListner.sfApiClientInitError(errorCode, errorMessage);
			}
		}
	};
	
	public void init(SFApiClientInitListener listener) throws SFInvalidStateException
	{
		mClientInitListner = listener;
		SFApiQuery<SFSession> sfQueryGetSession = SFSessionsEntity.get();				
		SFApiRunnable<SFSession> sfApiRunnable = new SFApiRunnable<SFSession>(SFSession.class,sfQueryGetSession, mListnererGetSession, mOAuthToken);
		sfApiRunnable.startNewThread();
	}

	public <T extends SFODataObject> void executeQuery(SFApiQuery<T> query , SFApiResponseListener<T> listener) throws SFInvalidStateException
	{								
		/*
		 *  See this error for why we need to store the inner class coxz of the java generics problem
		 * 
		if(query instanceof SFApiQuery<SFSession>)
		{
			
		}
		*/
		
		SFApiRunnable<T> sfApiRunnable = new SFApiRunnable<T>(query.getTrueInnerClass(),query, listener, mOAuthToken);
		sfApiRunnable.startNewThread();
	}
	
	private void validateStateBeforeInit() throws SFInvalidStateException
	{
		if(mOAuthToken == null)
		{
			throw new SFInvalidStateException(MSG_INVALID_STATE_OAUTH_NULL);
		}
	}
	
}
