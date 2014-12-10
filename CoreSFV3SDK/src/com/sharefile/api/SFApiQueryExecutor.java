package com.sharefile.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sharefile.api.authentication.SFOAuthTokenRenewer;
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
import com.sharefile.api.utils.Utils;
import com.sharefile.java.log.SLog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

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
	public SFODataObject executeBlockingQuery() throws SFV3ErrorException, SFInvalidStateException
    {


        synchronized (mSFApiClient)
        {

            mSFApiClient.validateClientState();

            //SLog.d(TAG, "executeBlockingQuery init with: [" + mSFApiClient.getOAuthToken().getAccessToken() + "]:[" + mSFApiClient.getOAuthToken().getRefreshToken() + "]");

            mResponse = new Response();

            int httpErrorCode = SFSDK.INTERNAL_HTTP_ERROR;
            String responseString = null;
            URLConnection connection = null;

            try {
                String server = mSFApiClient.getOAuthToken().getApiServer();
                String urlstr = mQuery.buildQueryUrlString(server);

                setCurrentUri(urlstr);

                URL url = new URL(urlstr);
                connection = SFHttpsCaller.getURLConnection(url);
                SFHttpsCaller.setMethod(connection, mQuery.getHttpMethod());
                mAppSpecificConfig.setAddtionalHeaders(connection);

                SFHttpsCaller.addAuthenticationHeader(connection, mSFApiClient.getOAuthToken(), mQuery.getUserName(), mQuery.getPassword(), mCookieManager);

                handleHttPost(connection);

                SLog.d(TAG, mQuery.getHttpMethod() + " " + urlstr);

                connection.connect();

                httpErrorCode = SFHttpsCaller.safeGetResponseCode(connection);

                SFHttpsCaller.getAndStoreCookies(connection, url, mCookieManager);

                switch (httpErrorCode) {
                    case HttpsURLConnection.HTTP_OK: {
                        responseString = SFHttpsCaller.readResponse(connection);
                        SLog.v(TAG, responseString);

                        SFODataObject ret = callSuccessResponseParser(responseString);

                        if (ret != null) {
                            mResponse.setResponse(ret, null);
                        } else {
                            if (mResponse.errorObject == null) {
                                mResponse.setResponse(null, new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR, null, null));
                            }
                        }
                    }
                    break;

                    case HttpsURLConnection.HTTP_NO_CONTENT: {
                        mResponse.setResponse(new SFNoContent(), null);
                    }
                    break;

                    case HttpsURLConnection.HTTP_UNAUTHORIZED: {
                        SLog.d(TAG, "RESPONSE = AUTH ERROR");

                        if (!mQuery.canReNewTokenInternally() || mAccessTokenRenewer == null) {
                            SFV3Error sfV3error = new SFV3Error(httpErrorCode, null, null);
                            mResponse.setResponse(null, sfV3error);
                        } else {
                            SFODataObject ret = executeQueryAfterTokenRenew();

                            if (ret != null) {
                                mResponse.setResponse(ret, null);
                            } else {
                                if (mResponse.errorObject == null) {
                                    mResponse.setResponse(null, new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR, null, null));
                                }
                            }
                        }
                    }
                    break;

                    default: {
                        responseString = SFHttpsCaller.readErrorResponse(connection);
                        SLog.v(TAG, responseString);
                        SFV3Error sfV3error = new SFV3Error(httpErrorCode, responseString, null);
                        mResponse.setResponse(null, sfV3error);
                    }
                }
            }
            catch (ConnectException ex)
            {
                SLog.e(TAG, ex);
                SFV3Error sfV3error = new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR_NETWORK_CONNECTION_PROBLEM, null, ex);
                mResponse.setResponse(null, sfV3error);
            }
            catch (Exception ex)
            {
                SLog.e(TAG, ex);
                SFV3Error sfV3error = new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR, null, ex);
                mResponse.setResponse(null, sfV3error);
            }
            catch (OutOfMemoryError e)
            {
                SLog.e(TAG, e.getLocalizedMessage());
                SFV3Error sfV3error = new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR, null, new SFOutOfMemoryException(e.getStackTrace().toString()));
                mResponse.setResponse(null, sfV3error);
            }
            finally
            {
                SFHttpsCaller.disconnect(connection);
            }

            callSaveCredentialsCallback(mResponse.returnObject, mResponse.errorObject);
        }

		return returnResultOrThrow(mResponse.returnObject,mResponse.errorObject);
	}		

    private void callSaveCredentialsCallback(SFODataObject sfobject,SFV3Error v3error)
    {
        if(mReauthHandler == null)
        {
            return;
        }

        //the auth was success. if the query had credentials, callback the caller to store those creds
        if(sfobject!=null)
        {
            if(!Utils.isEmpty(mQuery.getPassword()))
            {
                try
                {
                    mReauthHandler.storeCredentials(mQuery.getUserName(),mQuery.getPassword(),mQuery.getLink().toString());
                }
                catch (Exception e)
                {
                    SLog.e(TAG, "This can be dangerous if the caller cant store the credentials he might get prompted when cookies expire",e);
                }
            }
        }

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

    //https://crashlytics.com/citrix2/android/apps/com.sharefile.mobile.tablet/issues/5486913f65f8dfea154945c8/sessions/54834f7502e400013d029118062ebeab
    private boolean alreadyRenewedToken = false;
    private void logMultipleTokenRenewals()
    {
          if(!alreadyRenewedToken)
          {
              alreadyRenewedToken = true;
              return;
          }

          //Token already renewed once before in this query. dump logs
          SLog.e(TAG, "!!Multiple token renewals in same query. Might lead to stack overflow " +
                  "\n mCurrentUri =  " + mCurrentUri
                  + "\nmLink = " + mQuery.getLink());
    }

	private SFODataObject executeQueryAfterTokenRenew() throws SFV3ErrorException, SFInvalidStateException
    {
		if(!renewToken())
		{
			return null;
		}

        logMultipleTokenRenewals();

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

    private URI mCurrentUri = null;

    /**
        Use this to keep track of the current Uri on which the executor made an https call
        that way we can avoid making extra redirection calls.
     */
    private void setCurrentUri(String str)
    {
        try
        {
            mCurrentUri = new URI(str);
            //Want to control max number of redirects here?
        }
        catch(Exception e)
        {
            SLog.e(TAG,e);
        }
    }

    private boolean isNewRedirectionUri(SFRedirection redirection)
    {
        if(redirection == null)
        {
            return false;
        }

        if(mCurrentUri ==null || redirection.getUri()==null)
        {
            return false;
        }

        try
        {
            String currentHost = mCurrentUri.getHost();
            String currentPath = mCurrentUri.getPath();

            String targetHost = redirection.getUri().getHost();
            String targetPath = redirection.getUri().getPath();

            if (currentHost.equalsIgnoreCase(targetHost) && currentPath.equalsIgnoreCase(targetPath))
            {
                SLog.v(TAG, "Don't Redirect. Already fetched response from link " + redirection.getUri());
                return false;
            }
        }
        catch (Exception e)
        {
            SLog.e(TAG, "ZK folder might not show up correctly.",e);
            return false;
        }

        return true;
    }
	
	private SFRedirectionType redirectionRequired(SFODataObject object)
	{
		SFRedirectionType ret = SFRedirectionType.NONE;
		
		if(!mQuery.reDirectionAllowed())
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
			
			Boolean hasRemoteChildren = folder.getHasRemoteChildren();
			
			if(hasRemoteChildren!=null && hasRemoteChildren == true &&
                    isNewRedirectionUri(folder.getRedirection()))
			{					
				ret = SFRedirectionType.FOLDER_ENUM;
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
        if(v3error!=null && handleIfAuthError(v3error, mQuery))
        {
            return;
        }

		if(mResponseListener == null)
		{
			return;
		}
				
		try
		{
			if(v3error !=null)
			{
                mResponseListener.sfApiError(v3error, mQuery);
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

    //We want the related exceptions to be grouped on Crashlytics so throw them from different lines
    //Crashlytics groups Exceptions by [Filename , LineNumber]
    private void throwExceptionOnSeparateLines(SFV3Error error) throws SFV3ErrorException
    {
        SFV3ErrorException exception = new SFV3ErrorException(error);

        Exception containedException = error.getException();

        if(containedException == null)
        {
            throw exception;
        }

        //This is dumb but required.
        if(containedException instanceof ConnectException )
        {
            throw exception;
        }
        else if(containedException instanceof SFOutOfMemoryException)
        {
            throw exception;
        }
        else if(containedException instanceof UnsupportedEncodingException)
        {
            throw exception;
        }
        else if(containedException instanceof URISyntaxException)
        {
            throw exception;
        }
        else if(containedException instanceof UnknownHostException)
        {
            throw exception;
        }
        else if(containedException instanceof IOException )
        {
            throw exception;
        }
        else
        {
            throw exception;
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
			throwExceptionOnSeparateLines(v3error);
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