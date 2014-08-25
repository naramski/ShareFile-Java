package com.sharefile.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.sharefile.api.authentication.SFOAuthTokenRenewer;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFV3ErrorException;
import com.sharefile.api.https.SFApiFileDownloadRunnable;
import com.sharefile.api.https.SFApiFileUploadRunnable;
import com.sharefile.api.https.SFCookieManager;
import com.sharefile.api.interfaces.ISFApiExecuteQuery;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.interfaces.ISFReAuthHandler;
import com.sharefile.api.interfaces.SFApiDownloadProgressListener;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.interfaces.SFApiUploadProgressListener;
import com.sharefile.api.interfaces.SFAuthTokenChangeListener;
import com.sharefile.api.interfaces.SFGetNewAccessTokenListener;
import com.sharefile.api.models.SFDeviceStatus;
import com.sharefile.api.models.SFDownloadSpecification;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSession;
import com.sharefile.api.models.SFUploadSpecification;
import com.sharefile.api.utils.Utils;
import com.sharefile.java.log.SLog;

public class SFApiClient
{
	private static final String TAG = SFKeywords.TAG + "-SFApiClient";
	
	public static final String MSG_INVALID_STATE_OAUTH_NULL = "Invalid state: Oauth token not initialized for SFApiClient";
	
	private final AtomicReference<SFOAuth2Token> mOAuthToken = new AtomicReference<SFOAuth2Token>(null);
	private SFSession mSession = null;	
	private final SFCookieManager mCookieManager = new SFCookieManager(); 
	private final String mClientID;
	private final String mClientSecret;
	private final SFAuthTokenChangeListener mAuthTokenChangeListener;
	private String mSfUserId;
	
	private static final String DEFAULT_ACCEPTED_LANGUAGE = Utils.getAcceptLanguageString();
	
	private final SFConfiguration mSFAppConfig = new SFConfiguration();
			
	private final AtomicBoolean mClientInitializedSuccessFully = new AtomicBoolean(false);
	
	private SFOAuthTokenRenewer mOauthTokenRenewer;
	
	private final SFGetNewAccessTokenListener mGetNewAccessTokenListener = new SFGetNewAccessTokenListener()
	{
		@Override
		public void successGetAccessToken(SFOAuth2Token oAuthToken) 
		{
			try 
			{
				reinitClientState(oAuthToken);				
			} 
			catch (SFInvalidStateException e) 
			{				
				SLog.e(TAG,e);
			}						
		}

		@Override
		public void errorGetAccessToken(SFV3Error v3error) 
		{			
			SLog.e(TAG,v3error.errorDisplayString("error getting access token"));
		}		
	};
	
		
	public boolean isClientInitialised()
	{
		return mClientInitializedSuccessFully.get();
	}
	
	public SFOAuth2Token getOAuthToken()
	{
		return mOAuthToken.get();
	}
	
	public void clearAllCookies()
	{
		mCookieManager.clearAllCookies();
	}
	
	private void copyOAuthToken(SFOAuth2Token oauthToken) throws SFInvalidStateException
	{
		validateStateBeforeInit(oauthToken);
		
		mOAuthToken.set(oauthToken);
		
		mClientInitializedSuccessFully.set(true);
	}
	
	public SFApiClient(SFOAuth2Token oauthToken,String sfUserId,String clientID,String clientSecret, SFAuthTokenChangeListener listener) throws SFInvalidStateException
	{	
		mClientInitializedSuccessFully.set(false);		
		mAuthTokenChangeListener = listener;
		mClientID = clientID;
		mClientSecret = clientSecret;
		mSfUserId = sfUserId;
				
		mSFAppConfig.addAcceptedLanguage(DEFAULT_ACCEPTED_LANGUAGE);
		
		copyOAuthToken(oauthToken);					
		
		mOauthTokenRenewer = new SFOAuthTokenRenewer(mOAuthToken.get(), mGetNewAccessTokenListener, mClientID, mClientSecret);
	}
	
	/**
	 *   This function can be called only on clients which were previously initialized. 
	 *   This will internally call the token change listener allowing the creator of this object 
	 *   to store the new token to Persistant storage.
	 */
	@SFSDKDefaultAccessScope
	void reinitClientState(SFOAuth2Token oauthtoken) throws SFInvalidStateException
	{
		mClientInitializedSuccessFully.set(false);
		
		copyOAuthToken(oauthtoken);		
		
		mOauthTokenRenewer = new SFOAuthTokenRenewer(mOAuthToken.get(), mGetNewAccessTokenListener, mClientID, mClientSecret);
		
		if(mAuthTokenChangeListener!=null)
		{
			try
			{
				mAuthTokenChangeListener.sfApiStoreNewToken(oauthtoken);
			}
			catch(Exception e)
			{
				SLog.d(TAG, "Exception in initclient", e);
			}
		}
	}
						
	/**
	 * This will start a seperate thread to perform the operation and return immediately. Callers should use callback listeners to gather results
	 
	public synchronized <T extends SFODataObject> Thread executeQuery(ISFQuery<T> query , SFApiResponseListener<T> listener, ISFReAuthHandler reauthHandler) throws SFInvalidStateException
	{		
		return null;
	}
	*/
	
	public <T extends SFODataObject> ISFApiExecuteQuery getExecutor(ISFQuery<T> query , SFApiResponseListener<T> listener, ISFReAuthHandler reauthHandler) throws SFInvalidStateException
	{
		validateClientState();
		
		return new SFApiQueryExecutor<T>(this,query, listener, mOAuthToken.get(), mCookieManager, mSFAppConfig,mOauthTokenRenewer, reauthHandler);
	}
						
	/**
	 *   Make this a more stronger check than a simple null check on OAuth. 
	 */
	private void validateStateBeforeInit(SFOAuth2Token token) throws SFInvalidStateException
	{
		if(token == null)
		{
			throw new SFInvalidStateException(MSG_INVALID_STATE_OAUTH_NULL);
		}
		
		if(!token.isValid())
		{
			throw new SFInvalidStateException(MSG_INVALID_STATE_OAUTH_NULL);
		}
	}	
	
	private void validateClientState() throws SFInvalidStateException 
	{
		if(!mClientInitializedSuccessFully.get())
		{
			throw new SFInvalidStateException(MSG_INVALID_STATE_OAUTH_NULL);
		}
	}
	
	public SFSession getSession()
	{
		return mSession;
	}
				
	public Thread downloadFile(SFDownloadSpecification downloadSpecification,int resumeFromByteIndex, OutputStream outpuStream, SFApiDownloadProgressListener progressListener, String connUserName,String connPassword) throws SFInvalidStateException
	{
		validateClientState();
		
		SFApiFileDownloadRunnable sfDownloadFile = new SFApiFileDownloadRunnable(downloadSpecification, resumeFromByteIndex, outpuStream , this,progressListener,mCookieManager,connUserName,connPassword);
		return sfDownloadFile.startNewThread();				
	}
	
	public Thread uploadFile(SFUploadSpecification uploadSpecification,int resumeFromByteIndex, long tolalBytes, String destinationName, InputStream inputStream, SFApiUploadProgressListener progressListener, String connUserName,String connPassword, String details) throws SFInvalidStateException
	{
		validateClientState();
		
		SFApiFileUploadRunnable sfUploadFile = new SFApiFileUploadRunnable(uploadSpecification, resumeFromByteIndex, tolalBytes,destinationName, inputStream, this,progressListener,mCookieManager, connUserName,connPassword,details);
		return sfUploadFile.startNewThread();				
	}	
	
	/**
	 *  Resets the token and nulls the internal callbacks. Call this when you no longer want to use this object.
	 */
	public void reset()
	{		
		mClientInitializedSuccessFully.set(false);		
	}

	public String getUserId() 
	{		
		return mSfUserId;
	}
		
	public void setCookie(String urlStr, String cookieString) 
	{
		mCookieManager.storeAppSpecificCookies(urlStr, cookieString);
	}
	
	public void setCookie(URI uri, String cookieString) 
	{
		mCookieManager.storeAppSpecificCookies(uri, cookieString);		
	}
	
	public void removeCookies(String urlStr) 
	{
		mCookieManager.removeCookies(urlStr);
	}
	
	public void removeCookies(URI uri) 
	{
		mCookieManager.removeCookies(uri);
	}		
	
	/** 
	 *  The SDK itself does not use the userid. This id is simply sent back to the app with the reauth-context
	 *  so that the app can detect the user for which the re-auth was requested.
	 */
	public void setUserId(String sfUserid)
	{		
		mSfUserId = sfUserid;
	}
		
	public SFConfiguration getConfig()
	{
		return mSFAppConfig;
	}

	public SFODataObject executeQuery(SFApiQuery<SFDeviceStatus> query) throws SFV3ErrorException, SFInvalidStateException 
	{
		return getExecutor(query, null, null).executeBlockingQuery();		
	}
}