package com.sharefile.api;

import com.sharefile.api.interfaces.ISFReAuthHandler;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.utils.Utils;

/**
 *   This allows to call the getCredentials() functions on the original caller if they have implemented the ISFReAuthHandler interface
 */
@SuppressWarnings("rawtypes")
@DefaultAccessScope
class SFApiListenerReauthHandler implements SFApiResponseListener  
{
	private final SFApiResponseListener mOriginalListener;
	private final SFApiClient mSFApiClient;
	private final ISFReAuthHandler mReauthHandler;
	
	public SFApiListenerReauthHandler(SFApiResponseListener listener, ISFReAuthHandler reauthHandler, SFApiClient apiClient) 
	{
		mOriginalListener = listener;
		mSFApiClient = apiClient;
		mReauthHandler = reauthHandler;
	}
	
	@Override
	public void sfApiSuccess(SFODataObject object) 
	{
		Utils.safeCallSuccess(mOriginalListener, object);
	}
		
	@Override
	public void sfApiError(V3Error error, SFApiQuery sfapiApiqueri) 
	{		
		if(!handleIfAuthError(error, sfapiApiqueri))
		{
			Utils.safeCallErrorListener(mOriginalListener, error, sfapiApiqueri);
		}
	}
					
	private boolean handleIfAuthError(final V3Error error, final SFApiQuery sfapiApiqueri)
	{
		boolean ret = false;				
		if(error!=null && error.isAuthError()) 
		{
			//We explicitly check !sfapiApiqueri.canReNewTokenInternally() since we should never call the getCredentials for SFProvider.
			if( (mReauthHandler !=null && !sfapiApiqueri.canReNewTokenInternally()) )
			{								
				SFReAuthContext reauthContext = new SFReAuthContext(sfapiApiqueri, this, mSFApiClient); 
				mReauthHandler.getCredentials(reauthContext);
				ret = true;
			}			
		}
		
		return ret;
	}
}
