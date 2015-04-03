package com.sharefile.api;

import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.authentication.SFOAuthTokenRenewer;
import com.sharefile.api.constants.SFFolderID;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.entities.ISFEntities;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFNotAuthorizedException;
import com.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.sharefile.api.exceptions.SFOtherException;
import com.sharefile.api.exceptions.SFServerException;
import com.sharefile.api.https.SFCookieManager;
import com.sharefile.api.https.SFDownloadRunnable;
import com.sharefile.api.https.SFUploadRunnable;
import com.sharefile.api.https.TransferRunnable;
import com.sharefile.api.interfaces.IOAuthTokenChangeHandler;
import com.sharefile.api.interfaces.ISFApiResultCallback;
import com.sharefile.api.interfaces.ISFApiClient;
import com.sharefile.api.interfaces.ISFApiExecuteQuery;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.interfaces.ISFReAuthHandler;
import com.sharefile.api.log.Logger;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSession;
import com.sharefile.api.utils.Utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SFApiClient extends ISFEntities.Implementation implements ISFApiClient
{
	private static final String TAG = SFKeywords.TAG + "-SFApiClient";
	
	public static final String MSG_INVALID_STATE_OAUTH_NULL = "Invalid state: Oauth token not initialized for SFApiClient";
	
	private final AtomicReference<SFOAuth2Token> mOAuthToken = new AtomicReference<SFOAuth2Token>(null);
	private SFSession mSession = null;	
	private final SFCookieManager mCookieManager = new SFCookieManager(); 
	private final String mClientID;
	private final String mClientSecret;
	private final IOAuthTokenChangeHandler mAuthTokenChangeCallback;
	private String mSfUserId;
	
	private static final String DEFAULT_ACCEPTED_LANGUAGE = Utils.getAcceptLanguageString();
	
	private final SFConfiguration mSFAppConfig = new SFConfiguration();
			
	private final AtomicBoolean mClientInitializedSuccessFully = new AtomicBoolean(false);
	
	private SFOAuthTokenRenewer mOauthTokenRenewer;

    private final URI mDefaultTopUrl;

    private final ISFReAuthHandler mReAuthHandler;

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

    public SFApiClient(SFOAuth2Token oAuthToken) throws SFInvalidStateException
    {
        this(oAuthToken,
             null,
             SFSdk.getClientId(),
             SFSdk.getClientSecret(),
             SFSdk.getOAuthTokenChangeHandler(),
             SFSdk.getReAuthHandler());
    }

	public SFApiClient(SFOAuth2Token oauthToken,String sfUserId,String clientID,String clientSecret,
                       IOAuthTokenChangeHandler tokenChangeHandler, ISFReAuthHandler reAuthHandler) throws SFInvalidStateException
	{	
		mClientInitializedSuccessFully.set(false);		
		mAuthTokenChangeCallback = tokenChangeHandler;
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

        if(mAuthTokenChangeCallback !=null)
        {
            //Don't auto-renew the token if the client does not have handler to receive the changed token.
            mOauthTokenRenewer = new SFOAuthTokenRenewer(mOAuthToken.get(), mClientID, mClientSecret);
        }

        mReAuthHandler = reAuthHandler;
	}
	
	/**
	 *   This function can be called only on clients which were previously initialized. 
	 *   This will internally call the token change listener allowing the creator of this object 
	 *   to store the new token to Persistant storage.
	 */
	@SFSDKDefaultAccessScope
	void reInitClientState(SFOAuth2Token oauthtoken) throws SFInvalidStateException
	{
		mClientInitializedSuccessFully.set(false);
		
		copyOAuthToken(oauthtoken);

		if(mAuthTokenChangeCallback !=null)
		{
            //Don't auto-renew the token if the client does not have handler to receive the changed token.
            mOauthTokenRenewer = new SFOAuthTokenRenewer(mOAuthToken.get(), mClientID, mClientSecret);

			try
			{
                //give the app which created this SFClient object a chance to store the new token.
				mAuthTokenChangeCallback.storeNewToken(this,oauthtoken);
			}
			catch(Exception e)
			{
				Logger.d(TAG, "Exception in init apiClient", e);
			}
		}
	}

    @Override
	public synchronized <T> ISFApiExecuteQuery getExecutor(ISFQuery<T> query , ISFApiResultCallback<T> listener, ISFReAuthHandler reauthHandler) throws SFInvalidStateException
	{
		return new SFApiQueryExecutor<T>(this,query, listener, mCookieManager, mSFAppConfig,mOauthTokenRenewer, reauthHandler);
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

    @Override
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

	public <T extends SFODataObject> T executeQuery(ISFQuery<T> query) throws
            SFServerException, SFInvalidStateException,
            SFNotAuthorizedException, SFOAuthTokenRenewException, SFOtherException
    {
		return getExecutor(query, null, mReAuthHandler).executeBlockingQuery();
	}

    @Override
    public InputStream executeQuery(SFQueryStream query) throws
            SFServerException, SFInvalidStateException,
            SFNotAuthorizedException, SFOAuthTokenRenewException ,SFOtherException
    {
        return getExecutor(query, null, mReAuthHandler).executeBlockingQuery();
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
            Logger.e(TAG, e);
            return null;

        } catch (UnsupportedEncodingException e) {
            Logger.e(TAG, e);
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
     * @throws com.sharefile.api.exceptions.SFServerException
     */
    public SFUploadRunnable prepareUpload(String destinationName, String details, String v3Url, boolean overwrite, int resumeFromByteIndex, long tolalBytes,  InputStream inputStream, TransferRunnable.IProgress progressListener, String connUserName,String connPassword) throws SFInvalidStateException, SFServerException {
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

    @Override
    public void storeNewToken(ISFApiClient apiClient,SFOAuth2Token newAccessToken)
    {
        try
        {
            reInitClientState(newAccessToken);
            Logger.d(TAG, "!!!Re-init SFClient with new token");
        }
        catch (SFInvalidStateException e)
        {
            Logger.e(TAG,e);
        }
    }
}