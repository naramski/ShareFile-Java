package com.sharefile.api;

import com.sharefile.api.interfaces.ISFReAuthHandler;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.utils.Utils;

/**
 *   This allows to call the getCredentials() functions on the original caller if they have implemented the ISFReAuthHandler interface
 */
@DefaultAccessScope
class SFApiListenerReauthHandler<T extends SFODataObject> implements SFApiResponseListener<T>  
{
	private final SFApiResponseListener<T> mOriginalListener;
	private final SFApiClient mSFApiClient;
	private final ISFReAuthHandler mReauthHandler;
	
	public SFApiListenerReauthHandler(SFApiResponseListener<T> listener, ISFReAuthHandler reauthHandler, SFApiClient apiClient) 
	{
		mOriginalListener = listener;
		mSFApiClient = apiClient;
		mReauthHandler = reauthHandler;
	}
	
	@Override
	public void sfApiSuccess(T object) 
	{
		Utils.safeCallSuccess(mOriginalListener, object);
	}
		
	@Override
	public void sfApiError(V3Error error, SFApiQuery<T> sfapiApiqueri) 
	{		
		if(!handleIfAuthError(error, sfapiApiqueri))
		{
			Utils.safeCallErrorListener(mOriginalListener, error, sfapiApiqueri);
		}
	}
					
	private boolean handleIfAuthError(final V3Error error, final SFApiQuery<T> sfapiApiqueri)
	{
		boolean ret = false;				
		if(error!=null && error.isAuthError()) 
		{
			//We explicitly check !sfapiApiqueri.canReNewTokenInternally() since we should never call the getCredentials for SFProvider.
			if( (mReauthHandler !=null && !sfapiApiqueri.canReNewTokenInternally()) )
			{								
				SFReAuthContext<T> reauthContext = new SFReAuthContext<T>(sfapiApiqueri, this, mSFApiClient); 
				mReauthHandler.getCredentials(reauthContext);
				ret = true;
			}			
		}
		
		return ret;
	}
}
