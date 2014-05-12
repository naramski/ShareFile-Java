package com.sharefile.api;

import com.sharefile.api.authentication.SFGetNewAccessToken;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.interfaces.SFGetNewAccessTokenListener;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.utils.Utils;
import com.sharefile.java.log.SLog;

/**
 *   This listener works as a proxy for the api listener provided by the callers of the executeQuery.
 *   This intermediate listener allows us to trap the apiError and attempt to handle the intermediate reauthentication
 *   via auth token renewal if possible and restart the original query with the renewed token. This provides a centralized place
 *   where the authtoken for a user can get renewed and stored persistantly.
 */
@SFSDKDefaultAccessScope
class SFApiListenerTokenRenewer<T extends SFODataObject> implements SFApiResponseListener<T>
{
	private static final String TAG = SFKeywords.TAG + "-SFApiListenerTokenRenewer";
	private final SFApiListenerReauthHandler<T> mListener;
	private final SFApiQuery<T> mQuery;
	private final String mClientID;
	private final String mClientSecret;
	private final SFOAuth2Token mOAuthToken;
	private final SFBlockingWait mwaitForAuthResult = new SFBlockingWait();
	private final SFApiClient mApiClient;
	private final SFReturnWrapper<SFOAuth2Token> mNewTokenReturnValue = new SFReturnWrapper<SFOAuth2Token>();
	private final SFReturnWrapper<SFV3Error> mErrorReturnValue = new SFReturnWrapper<SFV3Error>();
	
	private SFGetNewAccessTokenListener mNewTokenListener = new SFGetNewAccessTokenListener() 
	{		
		@Override
		public void successGetAccessToken(SFOAuth2Token accessToken) 
		{	
			try
			{
				mNewTokenReturnValue.storeObject(accessToken);			
			
				mApiClient.reinitClientState(accessToken);
			}
			catch(Exception e)
			{
				SLog.d(TAG , "Exception despite success getting token", e);
			}
			
			mwaitForAuthResult.unblockWait();
		}
		
		@Override
		public void errorGetAccessToken(SFV3Error v3error) 
		{	
			SLog.d("token", "failed new token ");
			mErrorReturnValue.storeObject(v3error);
			mwaitForAuthResult.unblockWait();
		}
	};
	
	SFApiListenerTokenRenewer(SFApiClient client, SFApiListenerReauthHandler<T> listener, SFApiQuery<T> query,SFOAuth2Token oAuthToken ,String clientID,String clientSecret)
	{
		mListener = listener;
		mQuery = query;
		mClientID = clientID;
		mClientSecret = clientSecret;
		mOAuthToken = oAuthToken;
		mApiClient = client;
	}

	@Override
	public final void sfApiSuccess(T object) 
	{
		if(mListener!=null)
		{
			mListener.sfApiSuccess(object);
		}
	}
		
	@Override
	public final void sfApiError(final SFV3Error error, SFApiQuery<T> sfapiApiqueri) 
	{
		if(error.isAuthError() && sfapiApiqueri.canReNewTokenInternally())
		{
			SFGetNewAccessToken getNewToken = new SFGetNewAccessToken(mOAuthToken, mNewTokenListener, mClientID, mClientSecret);
			getNewToken.startNewThread();
			
			mwaitForAuthResult.blockingWait();
			
			if(mErrorReturnValue.getReturnObject()!=null)
			{								
				Utils.safeCallErrorListener(mListener, mErrorReturnValue.getReturnObject(), sfapiApiqueri);
			}
			else
			{
				try 
				{
					//restart the original query. Notice how we pass on the original listener and don't install our intermediate wrapper.
					// This is to avoid infinite recursion while trying handle the auth errors for tokens.
					mApiClient.executeQueryInternal(mQuery, mListener, false);
				} 
				catch (SFInvalidStateException e) 
				{					
					SLog.e(TAG,e);
				}
			}
		}
		else
		{
			Utils.safeCallErrorListener(mListener, error, sfapiApiqueri);			
		}
	}			
}