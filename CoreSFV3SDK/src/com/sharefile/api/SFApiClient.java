package com.sharefile.api;

import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.authentication.SFOAuthTokenRenewer;
import com.sharefile.api.constants.SFFolderID;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFV3ErrorException;
import com.sharefile.api.https.SFCookieManager;
import com.sharefile.api.https.SFDownloadRunnable;
import com.sharefile.api.https.SFUploadRunnable;
import com.sharefile.api.https.TransferRunnable;
import com.sharefile.api.interfaces.ISFApiClient;
import com.sharefile.api.interfaces.ISFApiExecuteQuery;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.interfaces.ISFReAuthHandler;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.interfaces.SFApiStreamResponse;
import com.sharefile.api.interfaces.SFAuthTokenChangeListener;
import com.sharefile.api.interfaces.SFGetNewAccessTokenListener;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSession;
import com.sharefile.api.utils.Utils;
import com.sharefile.java.log.SLog;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SFApiClient implements ISFApiClient
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

    private final URI mDefaultTopUrl;

	private final SFGetNewAccessTokenListener mGetNewAccessTokenListener = new SFGetNewAccessTokenListener()
	{
		@Override
		public void successGetAccessToken(SFOAuth2Token oAuthToken) 
		{
			try 
			{
				reinitClientState(oAuthToken);
                SLog.d(TAG, "!!!Re-init SFClient with new token");
			} 
			catch (SFInvalidStateException e) 
			{				
				SLog.e(TAG,e);
			}						
		}

		@Override
		public void errorGetAccessToken(SFV3Error v3error) 
		{			
			SLog.e(TAG,v3error.errorDisplayString("!!!error getting access token"));
            if(v3error.getHttpResponseCode() != SFSDK.INTERNAL_HTTP_ERROR)
            {
                reset();
            }

            if(mAuthTokenChangeListener!=null) 
            {
                mAuthTokenChangeListener.tokenRenewFailed(v3error);
            }

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
		SLog.d(TAG,"SFApiClient init with: [" + oauthToken.getAccessToken() + "]:["+oauthToken.getRefreshToken()+"]");//TODO-REMOVE-LOG
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

        try
        {
            mDefaultTopUrl = SFQueryBuilder.getDefaultURL(oauthToken.getSubdomain(),oauthToken.getApiCP(), SFFolderID.TOP);
        }
        catch (URISyntaxException e)
        {
            throw new SFInvalidStateException(e.getLocalizedMessage());
        }

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
				mAuthTokenChangeListener.storeNewToken(oauthtoken);
			}
			catch(Exception e)
			{
				SLog.d(TAG, "Exception in init apiClient", e);
			}
		}
	}
						
	
	public synchronized <T extends SFODataObject> ISFApiExecuteQuery getExecutor(ISFQuery<T> query , SFApiResponseListener<T> listener, ISFReAuthHandler reauthHandler) throws SFInvalidStateException
	{
		return new SFApiQueryExecutor<T>(this,query, listener, mCookieManager, mSFAppConfig,mOauthTokenRenewer, reauthHandler);
	}

    public synchronized ISFApiExecuteQuery getExecutor(ISFQuery<InputStream> query , SFApiStreamResponse listener) throws SFInvalidStateException
    {
        return new SFApiQueryExecutorForStreams(this,query, listener, mCookieManager, mSFAppConfig);
    }

	/**
	 *   Make this a more stronger check than a simple null check on OAuth. 
	 */
	@SFSDKDefaultAccessScope void validateStateBeforeInit(SFOAuth2Token token) throws SFInvalidStateException
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

	@SFSDKDefaultAccessScope void validateClientState() throws SFInvalidStateException
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

	public <T extends SFODataObject> T executeQuery(ISFQuery<T> query) throws SFV3ErrorException, SFInvalidStateException
	{
		return getExecutor(query, null, null).executeBlockingQuery();		
	}

    public InputStream executeQueryForStreams(ISFQuery<InputStream> query) throws SFV3ErrorException, SFInvalidStateException
    {
        return getExecutor(query,null).executeBlockingQuery();
    }

    //TODO_ADD_V3: This should be in SFDownloadRunnable
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
        String url;
        try  {
            ISFQuery<InputStream> downloadQuery = SFQueryBuilder.ITEMS.download(new URI(v3Url), true);//SFItemsEntity.download();
            downloadQuery.setLink(v3Url);
            String server = mOAuthToken.get().getApiServer();
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
     * prepare runnable to be used to upload a file
     * TODO_ADD_V3: needs to be moved to SFUploadRunnable.
     * create a different version that can handle prompting the users for connector credentials
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
    public SFUploadRunnable prepareUpload(String destinationName, String details, String v3Url, boolean overwrite, int resumeFromByteIndex, long tolalBytes,  InputStream inputStream, TransferRunnable.IProgress progressListener, String connUserName,String connPassword) throws SFInvalidStateException, SFV3ErrorException {
        validateClientState();

        return new SFUploadRunnable(v3Url, overwrite, resumeFromByteIndex, tolalBytes, destinationName, inputStream, this, progressListener, mCookieManager, connUserName, connPassword, details);
    }

    public URI getDefaultUrl(String folderID) throws URISyntaxException
    {
        if(SFFolderID.TOP.equalsIgnoreCase(folderID))
        {
            return mDefaultTopUrl;
        }

        return SFQueryBuilder.getDefaultURL(mOAuthToken.get().getSubdomain(),mOAuthToken.get().getApiCP(), folderID);
    }

    public URI getTopUrl() {
        return mDefaultTopUrl;
    }

    public URI getDeviceUrl(String deviceId) throws URISyntaxException
    {
        return SFQueryBuilder.getDeviceURL(getOAuthToken().getSubdomain(),
                getOAuthToken().getApiCP(),
                deviceId);
    }
}