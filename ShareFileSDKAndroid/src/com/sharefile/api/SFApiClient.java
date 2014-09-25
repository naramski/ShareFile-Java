package com.sharefile.api;

import com.sharefile.api.authentication.SFGetNewAccessToken;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.entities.SFItemsEntity;
import com.sharefile.api.enumerations.SFProvider;
import com.sharefile.api.exceptions.ReAuthException;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFV3ErrorException;
import com.sharefile.api.https.SFApiFileDownloadRunnable;
import com.sharefile.api.https.SFApiFileUploadRunnable;
import com.sharefile.api.https.SFApiRunnable;
import com.sharefile.api.https.SFCookieManager;
import com.sharefile.api.https.SFDownloadRunnable;
import com.sharefile.api.https.SFUploadRunnable;
import com.sharefile.api.https.TransferRunnable;
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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SFApiClient 
{
	private static final String TAG = SFKeywords.TAG + "-SFApiClient";
	
	public static final String MSG_INVALID_STATE_OAUTH_NULL = "Invalid state: Oauth token not initialized for SFApiClient";
	
	// private final AtomicReference<SFOAuth2Token> mOAuthToken = new AtomicReference<SFOAuth2Token>(null);
    //Dummy changes

	private SFSession mSession = null;	
	private final SFCookieManager mCookieManager = new SFCookieManager(); 
	private final String mClientID;
	private final String mClientSecret;
	private final SFAuthTokenChangeListener mAuthTokenChangeListener;
	private final String mSfUserId;
	
	private final AtomicBoolean mClientInitializedSuccessFully = new AtomicBoolean(false);
	
	public static final SFItemsEntity items = new SFItemsEntity();
	
	public boolean isClientInitialised()
	{
		return mClientInitializedSuccessFully.get();
	}
	
	public SFOAuth2Token getAuthToken()
	{
        return OAuthTokensCache.getInstance().get(mSfUserId);
	}
	
	public void clearAllCookies()
	{
		mCookieManager.clearAllCookies();
	}
	
	private void copyOAuthToken(SFOAuth2Token oauthToken) throws SFInvalidStateException
	{
		validateStateBeforeInit(oauthToken);
		
        OAuthTokensCache.getInstance().set(mSfUserId, oauthToken);
		
		mClientInitializedSuccessFully.set(true);
	}
	
	public SFApiClient(SFOAuth2Token oauthToken,String sfUserId,String clientID,String clientSecret, SFAuthTokenChangeListener listener) throws SFInvalidStateException
	{	
		mClientInitializedSuccessFully.set(false);
		
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
	public void reinitClientState(SFOAuth2Token oauthtoken) throws SFInvalidStateException
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
				SLog.d(TAG, "Failed to store token", e);
			}
		}
	}
						
	/**
	 * This will start a seperate thread to perform the operation and return immediately. Callers should use callback listeners to gather results
	 */
	public synchronized <T extends SFODataObject> Thread executeQuery(SFApiQuery<T> query , SFApiResponseListener<T> listener, ISFReAuthHandler reauthHandler) throws SFInvalidStateException
	{		
		SFApiListenerReauthHandler<T> sfReauthHandler = new SFApiListenerReauthHandler<T>(listener, reauthHandler, this,query);
		
		return executeQueryInternal(query, sfReauthHandler, true);
	}
	
	
	/**
	 *  We use this to install our own intermediate API listener. This allows us to handle the token renewal on auth Errors for 
	 *  ShareFile providers. This function has default access scope so that the SFApiListenerWrapper can call this to avoid 
	 *  the infinite recursion while attempting to handle auth errors.
	 */
	@SFSDKDefaultAccessScope
	<T extends SFODataObject> Thread executeQueryInternal(SFApiQuery<T> query , SFApiListenerReauthHandler<T> listener, boolean useTokenRenewer) throws SFInvalidStateException
	{
		validateClientState();
		
		SFApiResponseListener<T> targetLisner = listener;
		
		if(useTokenRenewer)
		{
			SFApiListenerTokenRenewer<T> listenereWrapper = new SFApiListenerTokenRenewer<T>(this,listener,query,getAuthToken(),mClientID,mClientSecret);
			targetLisner = listenereWrapper;
		}			
		
		SFApiRunnable<T> sfApiRunnable = new SFApiRunnable<T>(query, targetLisner, getAuthToken(),mCookieManager);
		return sfApiRunnable.startNewThread();
	}
	
	/**
	 * renew the oauth token synchronously. To be used on by asynctasks
	 * @return new oath token if successful, null if failed
	 */
	public boolean renewAccessTokenSync() throws SFInvalidStateException, ReAuthException {
		SFGetNewAccessToken task = new SFGetNewAccessToken(getAuthToken(), null, mClientID, mClientSecret);
		SFOAuth2Token newToken = task.getNewAccessToken();
        if ( newToken==null ) {
            // should not happen since we
            SLog.w(TAG, "Can't re-authenticate");
            return false;
        }

        SLog.i(TAG, "Access Token got renewed, update it");
        reinitClientState(newToken);

        return true;
	}
	
	public <T extends SFODataObject> T executeWithReAuth(SFApiQuery<T> query) throws SFV3ErrorException, SFInvalidStateException, ReAuthException {
		validateClientState();

		SFApiRunnable<T> sfApiRunnable =  null;
		try {
			sfApiRunnable = new SFApiRunnable<T>(query, null, getAuthToken(), mCookieManager);
			return sfApiRunnable.executeQuery();
			
		} catch ( SFV3ErrorException e) {
			if ( !e.isAuthException() ) throw e;
			if ( query.getProvider()!=SFProvider.PROVIDER_TYPE_SF ) {
				// Nothing to do since we assume this is being used on a non-interactive environment
				// TODO: allow for some call back so the user can enter new credentials?
				throw e;
			}
			if ( !renewAccessTokenSync() ) {
				SLog.w(TAG, "Failed to reauth");
				throw e;
			}
			SLog.i(TAG, "Repeat query after successful re-auth");
			return sfApiRunnable.executeQuery();
		}
		
	}
	
	/**
	 * This will block until operation is done and return the expected SFODataObject or throw a V3Exception. Never call this on UI thread.
	 */
	public <T extends SFODataObject> T executeQuery(SFApiQuery<T> query) throws SFV3ErrorException, SFInvalidStateException
	{										
		validateClientState();
		
		SFApiRunnable<T> sfApiRunnable = new SFApiRunnable<T>(query, null, getAuthToken(), mCookieManager);
		
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

	/**
	 * download file on separate thread
	 * @param downloadSpecification
	 * @param resumeFromByteIndex
	 * @param outpuStream
	 * @param progressListener
	 * @param connUserName
	 * @param connPassword
	 * @return
	 * @throws SFInvalidStateException
	 */
	@Deprecated
	public SFApiFileDownloadRunnable downloadFile(SFDownloadSpecification downloadSpecification,int resumeFromByteIndex, OutputStream outpuStream, SFApiDownloadProgressListener progressListener, String connUserName,String connPassword) throws SFInvalidStateException
	{
		validateClientState();
		
		SFApiFileDownloadRunnable sfDownloadFile = new SFApiFileDownloadRunnable(downloadSpecification, resumeFromByteIndex, outpuStream , this,progressListener,mCookieManager,connUserName,connPassword);
		sfDownloadFile.startNewThread();
		return sfDownloadFile;
	}
	
	/**
	 * create a runnable to handle downloading the file
	 * it is up to the developer to decide how to run this asynchronously (AsyncTask, Thread, ...) 
	 * @param itemId
	 * @param v3Url
	 * @param resumeFromByteIndex
	 * @param outpuStream
	 * @param progressListener
	 * @param connUserName
	 * @param connPassword
	 * @return
	 * @throws SFInvalidStateException
	 */
	public SFDownloadRunnable prepareDownload(String itemId, String v3Url, int resumeFromByteIndex, OutputStream outpuStream, TransferRunnable.IProgress progressListener, String connUserName, String connPassword) throws SFInvalidStateException {
		validateClientState();
		
		// calculate download URL
		String url = null;
		try  {
			SFApiQuery<SFDownloadSpecification> downloadQuery = SFItemsEntity.download(itemId, true);
			if ( v3Url!=null ) downloadQuery.setLink(v3Url);
			String server = getAuthToken().getApiServer();
			url = downloadQuery.buildQueryUrlString(server);
			
		} catch (URISyntaxException e)  {
			SLog.e(TAG, e);
			return null;
			
		} catch (UnsupportedEncodingException e) {
			SLog.e(TAG, e);
			return null;
		}
		
		// create runnable
		return new SFDownloadRunnable(url, resumeFromByteIndex, outpuStream , this, progressListener, mCookieManager, connUserName, connPassword);
	}
	
	/**
	 * Upload file on separate Thread
	 * @param uploadSpecification
	 * @param resumeFromByteIndex
	 * @param tolalBytes
	 * @param destinationName
	 * @param inputStream
	 * @param progressListener
	 * @param connUserName
	 * @param connPassword
	 * @param details
	 * @return
	 * @throws SFInvalidStateException
	 */
	@Deprecated
	public SFApiFileUploadRunnable uploadFile(SFUploadSpecification uploadSpecification,int resumeFromByteIndex, long tolalBytes, String destinationName, InputStream inputStream, SFApiUploadProgressListener progressListener, String connUserName,String connPassword, String details) throws SFInvalidStateException
	{
		validateClientState();
		
		SFApiFileUploadRunnable sfUploadFile = new SFApiFileUploadRunnable(uploadSpecification, resumeFromByteIndex, tolalBytes,destinationName, inputStream, this,progressListener,mCookieManager, connUserName,connPassword, details);
		sfUploadFile.startNewThread();
		return sfUploadFile;
	}	

	/**
	 * prepare runnable to be used to upload a file
	 * TODO: create a different version that can handle prompting the users for connector credentials
	 * @param parentId
	 * @param destinationName
	 * @param details
	 * @param v3Url
	 * @param overwrite
	 * @param resumeFromByteIndex
	 * @param tolalBytes
	 * @param inputStream
	 * @param progressListener
	 * @param connUserName
	 * @param connPassword
	 * @return
	 * @throws SFInvalidStateException
	 * @throws SFV3ErrorException
	 */
	public SFUploadRunnable prepareUpload(String parentId, String destinationName, String details, String v3Url, boolean overwrite, int resumeFromByteIndex, long tolalBytes,  InputStream inputStream, TransferRunnable.IProgress progressListener, String connUserName,String connPassword) throws SFInvalidStateException, SFV3ErrorException {
		validateClientState();

		return new SFUploadRunnable(parentId, v3Url, overwrite, resumeFromByteIndex, tolalBytes, destinationName, inputStream, this, progressListener, mCookieManager, connUserName, connPassword, details);
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
}