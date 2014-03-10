package com.sharefile.api;

import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.entities.SFSessionsEntity;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.https.SFApiRunnable;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFSession;

public class SFApiClient 
{
	private static final String TAG = "-SFApiClient";
	
	public static final String MSG_INVALID_STATE_OAUTH_NULL = "Invalid state: Oauth token not initialized for SFApiClient";
	
	private SFOAuth2Token mOAuthToken = null;
	private SFSession mSession = null;
	
	public SFApiClient(SFOAuth2Token outhToken) throws SFInvalidStateException
	{		
		mOAuthToken = outhToken;//TODO: Shouldn't this be cloned using a deep copy?	
		
		validateStateBeforeInit();
	}
		
	private SFApiResponseListener<SFSession> mListnererGetSession = new SFApiResponseListener<SFSession>() 
	{
		@Override
		public void sfapiSuccess(SFSession object) 
		{			
			mSession = object; //TODO: deep copy needed?
		}

		@Override
		public void sfApiError(int errorCode, String errorMessage,SFApiQuery<SFSession> asApiqueri) 
		{			
		}
	};
	
	public void init() throws SFInvalidStateException
	{
		SFApiQuery<SFSession> sfQueryGetSession = SFSessionsEntity.get();		
		SFApiRunnable<SFSession> sfApiRunnable = new SFApiRunnable<SFSession>(sfQueryGetSession, mListnererGetSession, mOAuthToken);
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
