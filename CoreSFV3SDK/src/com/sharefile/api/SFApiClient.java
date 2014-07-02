package com.sharefile.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.entities.SFItemsEntity;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFV3ErrorException;
import com.sharefile.api.https.SFApiFileDownloadRunnable;
import com.sharefile.api.https.SFApiFileUploadRunnable;
import com.sharefile.api.https.SFApiRunnable;
import com.sharefile.api.https.SFCookieManager;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.interfaces.ISFReAuthHandler;
import com.sharefile.api.interfaces.SFApiDownloadProgressListener;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.interfaces.SFApiUploadProgressListener;
import com.sharefile.api.interfaces.SFAuthTokenChangeListener;
import com.sharefile.api.models.SFDownloadSpecification;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSession;
import com.sharefile.api.models.SFUploadSpecification;
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
	private final String mSfUserId;
	private final Set<String> mAcceptedLanguages;
		
	private final AtomicBoolean mClientInitializedSuccessFully = new AtomicBoolean(false);
		
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
		mAcceptedLanguages = new HashSet<String>();
		mAuthTokenChangeListener = listener;
		mClientID = clientID;
		mClientSecret = clientSecret;
		mSfUserId = sfUserId;
		copyOAuthToken(oauthToken);					
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
	 */
	public synchronized <T extends SFODataObject> Thread executeQuery(ISFQuery<T> query , SFApiResponseListener<T> listener, ISFReAuthHandler reauthHandler) throws SFInvalidStateException
	{		
		return executeQuery(mSFApiRunnableClass, query, listener, reauthHandler);
	}
	
	/**
	 * This will start a seperate thread to perform the operation and return immediately. Callers should use callback listeners to gather results
	 */
	public synchronized <T extends SFODataObject> Thread executeQuery(Class<? extends SFApiRunnable> sfApiRunnableClass, ISFQuery<T> query , SFApiResponseListener<T> listener, ISFReAuthHandler reauthHandler) throws SFInvalidStateException
	{		
		SFApiListenerReauthHandler<T> sfReauthHandler = new SFApiListenerReauthHandler<T>(listener, reauthHandler, this,query);
		
		return executeQueryInternal(sfApiRunnableClass, query, sfReauthHandler, true);
	}
	
	/**
	 *  We use this to install our own intermediate API listener. This allows us to handle the token renewal on auth Errors for 
	 *  ShareFile providers. This function has default access scope so that the SFApiListenerWrapper can call this to avoid 
	 *  the infinite recursion while attempting to handle auth errors.
	 */
	@SFSDKDefaultAccessScope
	<T extends SFODataObject> Thread executeQueryInternal(Class<? extends SFApiRunnable> sfApiRunnableClass, ISFQuery<T> query , SFApiListenerReauthHandler<T> sfReauthHandler, boolean useTokenRenewer) throws SFInvalidStateException
	{
		validateClientState();
		
		SFApiResponseListener<T> targetLisner = sfReauthHandler;
		
		if(useTokenRenewer)
		{
			SFApiListenerTokenRenewer<T> listenereWrapper = new SFApiListenerTokenRenewer<T>(this,sfReauthHandler,query,mOAuthToken.get(),mClientID,mClientSecret); 							
			targetLisner = listenereWrapper;
		}			
		
		SFApiRunnable<T> sfApiRunnable;
		try {
			sfApiRunnable = newSFApiRunnable(sfApiRunnableClass, query, targetLisner);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			SLog.w(TAG, e.getLocalizedMessage(), e);
			sfApiRunnable = new SFApiRunnable<T>(query, targetLisner, mOAuthToken.get(),mCookieManager);
		}
		
		return sfApiRunnable.startNewThread();
	}
	
	/**
	 *  We use this to install our own intermediate API listener. This allows us to handle the token renewal on auth Errors for 
	 *  ShareFile providers. This function has default access scope so that the SFApiListenerWrapper can call this to avoid 
	 *  the infinite recursion while attempting to handle auth errors.
	 */
	@SFSDKDefaultAccessScope
	<T extends SFODataObject> Thread executeQueryInternal(ISFQuery<T> query , SFApiListenerReauthHandler<T> sfReauthHandler, boolean useTokenRenewer) throws SFInvalidStateException
	{
		return executeQueryInternal(mSFApiRunnableClass, query, sfReauthHandler, useTokenRenewer);
	}
	
	/**
	 * This will block until operation is done and return the expected SFODataObject or throw a V3Exception. Never call this on UI thread.
	 */
	public <T extends SFODataObject> SFODataObject executeQuery(SFApiQuery<T> query) throws SFV3ErrorException, SFInvalidStateException
	{										
		validateClientState();
		
		SFApiRunnable<T> sfApiRunnable = new SFApiRunnable<T>(query, null, mOAuthToken.get(),mCookieManager);
		
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
	
	public Thread uploadFile(SFUploadSpecification uploadSpecification,int resumeFromByteIndex, long tolalBytes, String destinationName, InputStream inputStream, SFApiUploadProgressListener progressListener, String connUserName,String connPassword) throws SFInvalidStateException
	{
		validateClientState();
		
		SFApiFileUploadRunnable sfUploadFile = new SFApiFileUploadRunnable(uploadSpecification, resumeFromByteIndex, tolalBytes,destinationName, inputStream, this,progressListener,mCookieManager, connUserName,connPassword);
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
	
	private Class<? extends SFApiRunnable> mSFApiRunnableClass = SFApiRunnable.class;
	
	public <T extends SFODataObject> void registerApiRunnableClass(Class<? extends SFApiRunnable<T>> clazz)
	{
		if(clazz==null)
			return ;
		
		mSFApiRunnableClass = clazz;
	}
	
	private <T extends SFODataObject> SFApiRunnable<T> newSFApiRunnable(Class<? extends SFApiRunnable> sfApiRunnableClass, ISFQuery<T> query, SFApiResponseListener<T> targetListner) throws SFInvalidStateException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Constructor ctor = sfApiRunnableClass.getConstructor(ISFQuery.class, SFApiResponseListener.class, SFOAuth2Token.class, SFCookieManager.class);
		Object obj = ctor.newInstance(query, targetListner, mOAuthToken.get(),mCookieManager);
		return (SFApiRunnable<T>) obj;
	}
	
	private <T extends SFODataObject> SFApiRunnable<T> newSFApiRunnable(ISFQuery<T> query, SFApiResponseListener<T> targetListner) throws SFInvalidStateException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		return newSFApiRunnable(mSFApiRunnableClass, query, targetListner);
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
	
	public void addAcceptLanguages(ArrayList<String> acceptedLanguages) 
	{
		mAcceptedLanguages.addAll(acceptedLanguages);
	}
	
	public void addAcceptLanguage(String acceptedLanguage) 
	{
		mAcceptedLanguages.add(acceptedLanguage);
	}
}