package com.sharefile.api;

import com.sharefile.api.authentication.SFGetNewAccessToken;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.interfaces.SFGetNewAccessTokenListener;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.utils.SFLog;

/**
 *   This listener works as a proxy for the api listener provided by the callers of the executeQuery.
 *   This intermediate listener allows us to trap the apiError and attempt to handle the intermediate reauthentication
 *   via auth token renewal if possible and restart the original query with the renewed token. This provides a centralixed place
 *   where the authtoken for a user can get renewed and stored persistantly.
 */
@SuppressWarnings("rawtypes")
class SFApiListenerWrapper implements SFApiResponseListener
{
	private final SFApiResponseListener<SFODataObject> mListener;
	private final SFApiQuery msfapiQuery;
	private final String mClientID;
	private final String mClientSecret;
	private final SFOAuth2Token mOAuthToken;
	private final SFBlockingWait mwaitForAuthResult = new SFBlockingWait();
	private final SFApiClient mApiClient;
	private final SFReturnWrapper<SFOAuth2Token> mNewTokenReturnValue = new SFReturnWrapper<SFOAuth2Token>();
	private final SFReturnWrapper<V3Error> mErrorReturnValue = new SFReturnWrapper<V3Error>();
	
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
				SFLog.d2("error", e.getLocalizedMessage());
			}
			
			mwaitForAuthResult.unblockWait();
		}
		
		@Override
		public void errorGetAccessToken(V3Error v3error) 
		{	
			SFLog.d2("token", "failed new token ");
			mErrorReturnValue.storeObject(v3error);
			mwaitForAuthResult.unblockWait();
		}
	};
	
	@SuppressWarnings("unchecked")
	SFApiListenerWrapper(SFApiClient client, SFApiResponseListener listener, SFApiQuery query,SFOAuth2Token oAuthToken ,String clientID,String clientSecret)
	{
		mListener = (SFApiResponseListener<SFODataObject>) listener;
		msfapiQuery = query;
		mClientID = clientID;
		mClientSecret = clientSecret;
		mOAuthToken = oAuthToken;
		mApiClient = client;
	}

	@Override
	public final void sfApiSuccess(SFODataObject object) 
	{
		if(mListener!=null)
		{
			mListener.sfApiSuccess(object);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final void sfApiError(final V3Error error, SFApiQuery sfapiApiqueri) 
	{
		if(error.isAuthError() && sfapiApiqueri.canhandleReAuthInternally())
		{
			SFGetNewAccessToken getNewToken = new SFGetNewAccessToken(mOAuthToken, mNewTokenListener, mClientID, mClientSecret);
			getNewToken.startNewThread();
			
			mwaitForAuthResult.blockingWait();
			
			if(mErrorReturnValue.getReturnObject()!=null)
			{
				if(mListener!=null)
				{
					mListener.sfApiError(mErrorReturnValue.getReturnObject(), sfapiApiqueri);
				}
			}
			else
			{
				try 
				{
					//restart the original query.
					mApiClient.executeQuery(msfapiQuery, mListener);
				} 
				catch (SFInvalidStateException e) 
				{					
					e.printStackTrace();
				}
			}
		}
		else
		{
			if(mListener!=null)
			{
				mListener.sfApiError(error, sfapiApiqueri);
			}
		}
	}			
}