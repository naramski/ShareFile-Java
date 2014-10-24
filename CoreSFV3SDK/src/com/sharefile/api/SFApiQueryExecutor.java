package com.sharefile.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sharefile.api.authentication.SFOAuthTokenRenewer;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.enumerations.SFRedirectionType;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFOutOfMemoryException;
import com.sharefile.api.exceptions.SFV3ErrorException;
import com.sharefile.api.gson.SFGsonHelper;
import com.sharefile.api.https.SFCookieManager;
import com.sharefile.api.https.SFHttpsCaller;
import com.sharefile.api.interfaces.ISFApiExecuteQuery;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.interfaces.ISFReAuthHandler;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFRedirection;
import com.sharefile.api.models.SFSymbolicLink;
import com.sharefile.api.utils.SFDumpLog;
import com.sharefile.java.log.SLog;

/**
 *  This class provides the bare-minimum functions to make the V3 API server calls and read + parse their responses.
 *  <br>These calls are blocking calls so that the application can use its own thread management.
 *  
 *  <br><br>The calls to be made are in this sequence:<br>
 *  
 *  <b>
 *  <br>executeBlockingQuery();
 *  <br>callresponseListeners();
 *  </b>
 *  
 *  <br><br>Typical usage in Android AsyncTask would be :<br>
 *  
 *  <br>doInBackgrond()
 *  <br>{
 *  <br>	executeBlockingQuery();
 *  <br>}
 *  <br>
 *  <br>onPostExecute()
 *  <br>{
 *  <br>	callresponseListeners();
 *  <br>}
 *  
 */
@SFSDKDefaultAccessScope 
class SFApiQueryExecutor<T extends SFODataObject> implements ISFApiExecuteQuery
{
	private static final String TAG = SFKeywords.TAG + "-SFApiThread";
			
	private final ISFQuery<T> mQuery; 
	private final SFApiResponseListener<T> mResponseListener;
	private final SFCookieManager mCookieManager;
	private final SFConfiguration mAppSpecificConfig;
	private final SFOAuthTokenRenewer mAccessTokenRenewer;
	private final ISFReAuthHandler mReauthHandler;
	private final SFApiClient mSFApiClient;	
				
	private final class Response
	{
		SFODataObject returnObject;
		SFV3Error errorObject;
		
		public void setResponse(SFODataObject ret,SFV3Error err)
		{
			returnObject = ret;
			errorObject = err;
		}
	}
	
	private Response mResponse = null;
						
	public SFApiQueryExecutor(SFApiClient apiClient, ISFQuery<T> query, SFApiResponseListener<T> responseListener, SFCookieManager cookieManager, SFConfiguration config, SFOAuthTokenRenewer tokenRenewer, ISFReAuthHandler reauthHandler) throws SFInvalidStateException
	{			
		mSFApiClient = apiClient;				
		mQuery = query;
		mResponseListener = responseListener;
		mCookieManager = cookieManager;
		mAppSpecificConfig = config;
		mAccessTokenRenewer = tokenRenewer;
		mReauthHandler = reauthHandler;
	}
		
	private void handleHttPost(URLConnection conn) throws IOException
	{
		if(mQuery.getHttpMethod().equalsIgnoreCase(SFHttpMethod.POST.toString()) || 
		   mQuery.getHttpMethod().equalsIgnoreCase(SFHttpMethod.PATCH.toString()) )
		{
			String body = mQuery.getBody(); 
			
			if(body!=null)
			{
				conn.setRequestProperty(SFKeywords.CONTENT_LENGTH, ""+body.getBytes().length);
				conn.setRequestProperty(SFKeywords.CONTENT_TYPE, SFKeywords.APPLICATION_JSON);
				
				SFHttpsCaller.postBody(conn, body);				
			}
		}
	}			
	
	@Override
	public void callResponseListeners() throws SFInvalidStateException 
	{
		if(mResponse == null)
		{
			throw new SFInvalidStateException("The Application needs to call executeBlockingQuery() before calling responselistener.");
		}
		
		callResponseListeners(mResponse.returnObject, mResponse.errorObject);
	}

    /**
        This call has to be synchronized to protect from the OAuthToken renewal problems otherwise
        it nmay happen that two parellel threads invoke this function, receive 401 for ShareFile
        and one of them renews the OAuthToken leaving the other one with a stale copy.
     */
	@Override
	public synchronized SFODataObject executeBlockingQuery() throws SFV3ErrorException, SFInvalidStateException
    {
        mSFApiClient.validateClientState();

        SLog.v(TAG,"executeBlockingQuery init with: [" + mSFApiClient.getOAuthToken().getAccessToken() + "]:["+mSFApiClient.getOAuthToken().getRefreshToken()+"]");//TODO-REMOVE-LOG

		mResponse = new Response();
		
		int httpErrorCode =  SFSDK.INTERNAL_HTTP_ERROR;
		String responseString = null;
		URLConnection connection = null;		
		
		try
		{						
			String server = mSFApiClient.getOAuthToken().getApiServer();
			String urlstr = mQuery.buildQueryUrlString(server);
								
			URL url = new URL(urlstr);
			connection = SFHttpsCaller.getURLConnection(url);		
			SFHttpsCaller.setMethod(connection, mQuery.getHttpMethod());
			mAppSpecificConfig.setAddtionalHeaders(connection);
			
			SFHttpsCaller.addAuthenticationHeader(connection,mSFApiClient.getOAuthToken(),mQuery.getUserName(),mQuery.getPassword(),mCookieManager);
			
			handleHttPost(connection);
			
			SLog.d(TAG, mQuery.getHttpMethod() + " " + urlstr);
			
			connection.connect();
			
			httpErrorCode = SFHttpsCaller.safeGetResponseCode(connection);			
						
			SFHttpsCaller.getAndStoreCookies(connection, url,mCookieManager);
			
			switch(httpErrorCode)
			{
				case HttpsURLConnection.HTTP_OK:
				{															
					responseString = SFHttpsCaller.readResponse(connection);
					SFDumpLog.dumpLog(TAG, "RAW RESPONSE = " , responseString);
					
					SFODataObject ret = callSuccessResponseParser(responseString);
					
					if(ret!=null)
					{
						mResponse.setResponse(ret, null);
					}
					else
					{
						if(mResponse.errorObject == null)
						{
							mResponse.setResponse(null, new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR,null,null));
						}
					}
				}
				break;
				
				case HttpsURLConnection.HTTP_NO_CONTENT:
				{
					mResponse.setResponse(new SFNoContent(), null);
				}
				break;
				
				case HttpsURLConnection.HTTP_UNAUTHORIZED:
				{
					SLog.d(TAG, "RESPONSE = AUTH ERROR");
					
					if(!mQuery.canReNewTokenInternally() || mAccessTokenRenewer==null)
					{
						SFV3Error sfV3error = new SFV3Error(httpErrorCode,null,null);
						mResponse.setResponse(null, sfV3error);
					}
					else
					{												
						SFODataObject ret = executeQueryAfterTokenRenew();
						
						if(ret!=null)
						{
							mResponse.setResponse(ret, null);
						}
						else
						{
							if(mResponse.errorObject == null)
							{
								mResponse.setResponse(null, new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR,null,null));
							}
						}
					}
				}
				break;
				
				default:
				{
					responseString = SFHttpsCaller.readErrorResponse(connection);
					SFDumpLog.dumpLog(TAG, "RAW RESPONSE = " , responseString);
					SFV3Error sfV3error = new SFV3Error(httpErrorCode,responseString,null);
					mResponse.setResponse(null, sfV3error);
				}
			}		    							    															
		}
		catch(Exception ex)
		{		
			SLog.e(TAG,ex);
			SFV3Error sfV3error = new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR, null, ex);
			mResponse.setResponse(null, sfV3error);
		}		
		catch (OutOfMemoryError e) 
		{
			SLog.e(TAG,e.getLocalizedMessage());
			SFV3Error sfV3error = new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR, null, new SFOutOfMemoryException(e.getStackTrace().toString()));
			mResponse.setResponse(null, sfV3error);
		}
		finally
		{
			SFHttpsCaller.disconnect(connection);
		}
								
		return returnResultOrThrow(mResponse.returnObject,mResponse.errorObject);
	}		
	
	private boolean renewToken() throws SFV3ErrorException
	{
        SLog.d(TAG, "!!!Trying to renew token");

		if(mAccessTokenRenewer==null)
		{
            SLog.d(TAG, "!!!no token renewer");
			return false;
		}

        boolean ret = false;

		if(mAccessTokenRenewer.getNewAccessToken() != null)
		{
            SLog.d(TAG, "!!!renewed token successfuly");
            ret = true;
		}
        else
        {
            SLog.e(TAG, "!!!token renew failed due to: " + mAccessTokenRenewer.getError().errorDisplayString("unknown"));
            mResponse.setResponse(null, mAccessTokenRenewer.getError());
        }

        mAccessTokenRenewer.callResponseListeners();

        return ret;
	}
	
	private SFODataObject executeQueryAfterTokenRenew() throws SFV3ErrorException, SFInvalidStateException
    {
		if(!renewToken())
		{
			return null;
		}
		
		return executeBlockingQuery();
	}

	private SFODataObject executeQueryOnRedirectedObject(SFRedirection redirection) throws SFInvalidStateException, SFV3ErrorException
    {
		SFODataObject odataObject = null;

        try
        {
            URI redirectLink = redirection.getUri();
            SLog.d(TAG,"REDIRECT TO: " + redirectLink);
            mQuery.setLinkAndAppendPreviousParameters(redirectLink);
        }
        catch (NullPointerException e)
        {
            SLog.e(TAG,e);
            throw new RuntimeException("Server Bug: Redirection object or Uri is null");
        }
        catch (URISyntaxException e)
        {
            SLog.e(TAG,e);
            throw new RuntimeException("Server Bug: Redirection object syntax error");
        }
        catch (UnsupportedEncodingException e)
        {
            SLog.e(TAG,e);
            throw new RuntimeException("Server Bug: Redirection object unsupported encoding");
        }

        odataObject = executeBlockingQuery();
		
		return odataObject;
	}
		
	/**
	 *   If the returned object is a Symbolic Link or has a Redirection feild we need to read ahead to get the actual contents.
	 * @throws URISyntaxException 
	 * @throws SFV3ErrorException 
	 *   
	 */
	
	private boolean mAlreadRedirecting = false;
	
	private SFRedirectionType redirectionRequired(SFODataObject object)
	{
		SFRedirectionType ret = SFRedirectionType.NONE;
		
		if(!mQuery.readAheadAllowed())
		{
			return ret;
		}
		
		if((object == null) )
		{
			return ret;
		}
				
		if(object instanceof SFSymbolicLink)
		{
			ret = SFRedirectionType.SYMBOLIC_LINK;
		}
		else if(object instanceof SFFolder)
		{
			SFFolder folder = (SFFolder) object;
			
			Boolean hadRemoteChildren = folder.getHasRemoteChildren();
			
			if(folder.getRedirection()!=null && hadRemoteChildren!=null && hadRemoteChildren == true && !mAlreadRedirecting)
			{					
				ret = SFRedirectionType.FOLDER_ENUM;
				mAlreadRedirecting = true;
			}
		}
		else if(object instanceof SFRedirection)
		{
			ret = SFRedirectionType.URI;				
		}		
		
		return ret;
	}
	
	private SFODataObject executeQueryOnSymbolicLink(SFSymbolicLink link) throws URISyntaxException, SFV3ErrorException, UnsupportedEncodingException, SFInvalidStateException
    {
		mQuery.setLinkAndAppendPreviousParameters(link.getLink());		
		return executeBlockingQuery();
	}
	
	
	private boolean handleIfAuthError(final SFV3Error error, final ISFQuery<T> sfapiApiqueri)
	{
		boolean ret = false;				
		if(error!=null && error.isAuthError()) 
		{
			//We explicitly check !sfapiApiqueri.canReNewTokenInternally() since we should never call the getCredentials for SFProvider.
			if( (mReauthHandler !=null && !sfapiApiqueri.canReNewTokenInternally()) )
			{								
				SFReAuthContext<T> reauthContext = new SFReAuthContext<T>(sfapiApiqueri, mResponseListener,mReauthHandler, mSFApiClient); 
				mReauthHandler.getCredentials(reauthContext);
				ret = true;
			}			
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	protected void callResponseListeners(SFODataObject sfobject,SFV3Error v3error)
	{
		if(mResponseListener == null)
		{
			return;
		}
				
		try
		{
			if(v3error !=null)
			{
				if(!handleIfAuthError(v3error, mQuery))
				{
					mResponseListener.sfApiError(v3error, mQuery);
				}
			}
			else
			{
				mResponseListener.sfApiSuccess((T) sfobject);
			}						
		}
		catch(Exception ex)
		{
			SLog.e(TAG,ex);
		}
	}
	
	private SFODataObject returnResultOrThrow(SFODataObject sfobject,SFV3Error v3error) throws SFV3ErrorException
	{
		if(sfobject!=null)
		{		
			return sfobject;
		}
		
		if(mResponseListener == null)
		{		
			throw new SFV3ErrorException(v3error);
		}
		
		return null;
	}
				
				
	/**
	 *  If an error happens during parsing the success response, 
	 *  we return the exception description + the original server response in V3Error Object
	 * @throws URISyntaxException 
	 * @throws SFV3ErrorException 
	 * @throws UnsupportedEncodingException 
	 */
	protected SFODataObject callSuccessResponseParser(String responseString) throws SFV3ErrorException, URISyntaxException, UnsupportedEncodingException, SFInvalidStateException
    {
		JsonParser jsonParser = new JsonParser();
		JsonElement jsonElement =jsonParser.parse(responseString);
		SFODataObject sfobject = SFGsonHelper.customParse(jsonElement);
				
		switch (redirectionRequired(sfobject)) 
		{
			case SYMBOLIC_LINK:
				sfobject = executeQueryOnSymbolicLink((SFSymbolicLink)sfobject);
			break;
					
			case FOLDER_ENUM:	
				sfobject = executeQueryOnRedirectedObject(((SFFolder)sfobject).getRedirection());
			break;
				
			case URI:
				sfobject = executeQueryOnRedirectedObject((SFRedirection) sfobject);							
			break;
				
			case NONE:
				//do nothing
			break;
		}
				
		return sfobject;
	}
			
	protected SFApiResponseListener<T> getResponseListener()
	{
		return mResponseListener;
	}			
}