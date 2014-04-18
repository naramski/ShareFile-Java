package com.sharefile.api;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.entities.SFItemsEntity;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFV3ErrorException;
import com.sharefile.api.https.SFApiFileDownloadRunnable;
import com.sharefile.api.https.SFApiFileUploadRunnable;
import com.sharefile.api.https.SFApiRunnable;
import com.sharefile.api.https.SFCookieManager;
import com.sharefile.api.interfaces.SFApiDownloadProgressListener;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.interfaces.SFApiUploadProgressListener;
import com.sharefile.api.models.SFDownloadSpecification;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSession;
import com.sharefile.api.models.SFUploadSpecification;

public class SFApiClient 
{
	private static final String TAG = "-SFApiClient";
	
	public static final String MSG_INVALID_STATE_OAUTH_NULL = "Invalid state: Oauth token not initialized for SFApiClient";
	
	private SFOAuth2Token mOAuthToken = null;
	private SFSession mSession = null;	
	private final SFCookieManager mCookieManager = new SFCookieManager(); 
	private final String mClientID;
	private final String mClientSecret;
	
	private boolean mClientInitializedSuccessFully = false;
	
	public static final SFItemsEntity items = new SFItemsEntity();
	
	public boolean isClientInitialised()
	{
		return mClientInitializedSuccessFully;
	}
	
	public SFOAuth2Token getAuthToken()
	{
		return mOAuthToken;
	}
	
	private void copyOAuthToken(SFOAuth2Token oauthToken) throws SFInvalidStateException
	{
		validateStateBeforeInit(oauthToken);
		
		mOAuthToken = oauthToken;
		
		mClientInitializedSuccessFully = true;
	}
	
	public SFApiClient(SFOAuth2Token oauthToken,String clientID,String clientSecret) throws SFInvalidStateException
	{	
		mClientInitializedSuccessFully = false;
		
		mClientID = clientID;
		mClientSecret = clientSecret;
		copyOAuthToken(oauthToken);					
	}
	
	public void reinitClientState(SFOAuth2Token oauthtoken) throws SFInvalidStateException
	{
		mClientInitializedSuccessFully = false;
		
		copyOAuthToken(oauthtoken);				
	}
						
	/**
	 * This will start a seprate thread to perform the operation and return immediately. Callers should use callback listeners to gather results
	 */
	public <T extends SFODataObject> Thread executeQuery(SFApiQuery<T> query , SFApiResponseListener<T> listener) throws SFInvalidStateException
	{										
		validateClientState();
		
		SFApiListenerWrapper listenereWrapper = new SFApiListenerWrapper(this,listener,query,mOAuthToken,mClientID,mClientSecret); 
				
		SFApiRunnable<T> sfApiRunnable = new SFApiRunnable<T>(query, listenereWrapper, mOAuthToken,mCookieManager);
		return sfApiRunnable.startNewThread();
	}
	
	/**
	 * This will block until operation is done and return the expected SFODataObject or throw a V3Exception. Never call this on UI thread.
	 */
	public <T extends SFODataObject> SFODataObject executeQuery(SFApiQuery<T> query) throws SFV3ErrorException, SFInvalidStateException
	{										
		validateClientState();
		
		SFApiRunnable<T> sfApiRunnable = new SFApiRunnable<T>(query, null, mOAuthToken,mCookieManager);
		
		return sfApiRunnable.executeQuery();
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
		if(!mClientInitializedSuccessFully)
		{
			throw new SFInvalidStateException(MSG_INVALID_STATE_OAUTH_NULL);
		}
	}
	
	public SFSession getSession()
	{
		return mSession;
	}
				
	public Thread downloadFile(SFDownloadSpecification downloadSpecification,int resumeFromByteIndex, FileOutputStream fileOutpuStream, SFApiDownloadProgressListener progressListener) throws SFInvalidStateException
	{
		validateClientState();
		
		SFApiFileDownloadRunnable sfDownloadFile = new SFApiFileDownloadRunnable(downloadSpecification, resumeFromByteIndex, fileOutpuStream , this,progressListener,mCookieManager);
		return sfDownloadFile.startNewThread();				
	}
	
	public Thread uploadFile(SFUploadSpecification uploadSpecification,int resumeFromByteIndex, long tolalBytes, String destinationName, FileInputStream fileInputStream, SFApiUploadProgressListener progressListener) throws SFInvalidStateException
	{
		validateClientState();
		
		SFApiFileUploadRunnable sfUploadFile = new SFApiFileUploadRunnable(uploadSpecification, resumeFromByteIndex, tolalBytes,destinationName, fileInputStream, this,progressListener,mCookieManager);
		return sfUploadFile.startNewThread();				
	}	
	
	/**
	 *  Resets the token and nulls the internal callbacks. Call this when you no longer want to use this object.
	 */
	public void reset()
	{		
		mClientInitializedSuccessFully = false;		
	}
}