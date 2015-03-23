package com.sharefile.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sharefile.api.authentication.SFOAuthTokenRenewer;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.enumerations.SFRedirectionType;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFNotAuthorizedException;
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
import com.sharefile.api.models.SFRedirection;
import com.sharefile.api.utils.Utils;
import com.sharefile.java.log.SLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

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
class SFApiQueryExecutor<T> implements ISFApiExecuteQuery
{
	private static final String TAG = SFKeywords.TAG + "-SFApiThread";
			
	private final ISFQuery<T> mQuery; 
	private final SFApiResponseListener<T> mResponseListener;
	private final SFCookieManager mCookieManager;
	private final SFConfiguration mAppSpecificConfig;
	private final SFOAuthTokenRenewer mAccessTokenRenewer;
	private final ISFReAuthHandler mReauthHandler;
	private final SFApiClient mSFApiClient;	

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

    private boolean shouldGetInputStream()
    {
        return (mQuery instanceof SFQueryStream);

    }

    private InputStream getInputStream(URLConnection connection, int httpErrorCode) throws IOException
    {
        // normally, 3xx is redirect
        if (httpErrorCode != HttpsURLConnection.HTTP_OK)
        {
            if (httpErrorCode        == HttpsURLConnection.HTTP_MOVED_TEMP
                    || httpErrorCode == HttpsURLConnection.HTTP_MOVED_PERM
                    || httpErrorCode == HttpsURLConnection.HTTP_SEE_OTHER)
            {
                String newUrl = connection.getHeaderField("Location");

                if(Utils.isEmpty(newUrl))
                {
                    newUrl = connection.getHeaderField("location");
                }

                SLog.d(TAG, "Redirect to: "+ newUrl);

                connection = SFHttpsCaller.getURLConnection(new URL(newUrl));

                SFHttpsCaller.addAuthenticationHeader(connection,
                        mSFApiClient.getOAuthToken(),
                        mQuery.getUserName(),
                        mQuery.getPassword(),
                        mCookieManager);

                connection.connect();

                return connection.getInputStream();
            }
        }

        return null;
    }

    private T executeQueryWithReAuthentication() throws SFV3ErrorException, SFNotAuthorizedException, SFInvalidStateException
    {
        if (mQuery.canReNewTokenInternally())
        {
            if(mAccessTokenRenewer == null)
            {
                throw new SFNotAuthorizedException("Not Authorized (401)");
            }

            return executeQueryAfterTokenRenew();
        }
        else
        {
            if(mReauthHandler != null)
            {
                SFCredential creds = mReauthHandler.getCredentials(mQuery.getLink().toString(),mSFApiClient);
                if(creds!=null && creds.getUserName()!=null && creds.getPassword()!=null)
                {
                    mQuery.setCredentials(creds.getUserName(),creds.getPassword());
                    return executeBlockingQuery();
                }
            }

            throw new SFNotAuthorizedException("Not Authorized (401)");
        }
    }

    private void throwException(SFV3ErrorException ex) throws SFV3ErrorException
    {
        SLog.e(TAG,ex);
        throw ex;
    }

    /**
        This call has to be synchronized to protect from the OAuthToken renewal problems otherwise
        it nmay happen that two parellel threads invoke this function, receive 401 for ShareFile
        and one of them renews the OAuthToken leaving the other one with a stale copy.
     */
	@Override
	public T executeBlockingQuery() throws SFV3ErrorException, SFInvalidStateException
    {
        synchronized (mSFApiClient)
        {

            mSFApiClient.validateClientState();

            int httpErrorCode;
            String responseString;
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

                if(shouldGetInputStream())
                {
                    return (T)getInputStream(connection,httpErrorCode);
                }

                switch (httpErrorCode)
                {
                    case HttpsURLConnection.HTTP_OK:
                    {
                        responseString = SFHttpsCaller.readResponse(connection);
                        SLog.v(TAG, responseString);

                        T ret = callSuccessResponseParser(responseString);
                        callSaveCredentialsCallback(ret);
                        return ret;
                    }
                    //break;

                    case HttpsURLConnection.HTTP_NO_CONTENT:
                    {
                        return null;
                    }
                    //break;

                    case HttpsURLConnection.HTTP_UNAUTHORIZED:
                    {
                        SLog.d(TAG, "RESPONSE = AUTH ERROR");

                        callWipeCredentialsCallback();

                        return executeQueryWithReAuthentication();
                    }
                    //break;

                    default:
                    {
                        responseString = SFHttpsCaller.readErrorResponse(connection);
                        SLog.v(TAG, responseString);
                        SFV3Error sfV3error = new SFV3Error(httpErrorCode, responseString, null);
                        throwException(new SFV3ErrorException(sfV3error));
                    }
                }
            }
            catch (ConnectException ex)
            {
                SFV3Error sfV3error = new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR_NETWORK_CONNECTION_PROBLEM, null, ex);
                throwException(new SFV3ErrorException(sfV3error));
            }
            catch(SSLException ex)
            {
                SFV3Error sfV3error = new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR_NETWORK_CONNECTION_PROBLEM, null, ex);
                throwException(new SFV3ErrorException(sfV3error));
            }
            catch (OutOfMemoryError e)
            {
                SFV3Error sfV3error = new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR, null, new SFOutOfMemoryException(Arrays.toString(e.getStackTrace())));
                throwException(new SFV3ErrorException(sfV3error));
            }
            catch (Exception ex)
            {
                SFV3Error sfV3error = new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR, null, ex);
                throwException(new SFV3ErrorException(sfV3error));
            }
            finally
            {
                SFHttpsCaller.disconnect(connection);
            }
        }

        //This should never happen
        SLog.e(TAG,"This should never happen as a result of SDK executeBlockingQuery");
        return null;
	}		

    private void callSaveCredentialsCallback(T sfobject)
    {
        if(mReauthHandler == null || sfobject==null)
        {
            return;
        }

        //the auth was success. if the query had credentials, callback the caller to store those creds
        if(!Utils.isEmpty(mQuery.getPassword()))
        {
            try
            {
                mReauthHandler.storeCredentials(new SFCredential(mQuery.getUserName(),
                        mQuery.getPassword()),mQuery.getLink().toString(),mSFApiClient);
            }
            catch (Exception e)
            {
                SLog.e(TAG, "This can be dangerous if the caller cant store the credentials he might get prompted when cookies expire",e);
            }
        }
    }

    private void callWipeCredentialsCallback()
    {
        if(mReauthHandler == null)
        {
            return;
        }

        //the auth was failure. if the query had credentials, callback the caller to wipe those creds.
        if(!Utils.isEmpty(mQuery.getPassword()))
        {
            try
            {
                SLog.d(TAG, "The stored credentials don't work anymore! Wipe them!");
                mReauthHandler.wipeCredentials(mQuery.getLink().toString(),mSFApiClient);
                mQuery.setCredentials(null,null);
            }
            catch (Exception e)
            {
                SLog.e(TAG, "This can be dangerous if the caller cant store the credentials he might get prompted when cookies expire",e);
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
            throw new SFV3ErrorException(mAccessTokenRenewer.getError());
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

	private T executeQueryAfterTokenRenew() throws SFV3ErrorException, SFInvalidStateException
    {
		if(!renewToken())
		{
			return null;
		}

        logMultipleTokenRenewals();

		return executeBlockingQuery();
	}

	private T executeQueryOnRedirectedObject(SFRedirection redirection) throws SFInvalidStateException, SFV3ErrorException
    {
		T odataObject;

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
	
	private SFRedirectionType redirectionRequired(T object)
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

        /*
		if(object instanceof SFSymbolicLink)
		{
			ret = SFRedirectionType.SYMBOLIC_LINK;
		}
		*/
		else if(object instanceof SFFolder)
		{
			SFFolder folder = (SFFolder) object;
			
			Boolean hasRemoteChildren = folder.getHasRemoteChildren();
			
			if(hasRemoteChildren!=null && hasRemoteChildren &&
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

    //We want the related exceptions to be grouped on Crashlytics so throw them from different lines
    //Crashlytics groups Exceptions by [Filename , LineNumber], new Exeception captures the
    //stack trace of the given location.
    private void throwExceptionOnSeparateLines(SFV3Error error) throws SFV3ErrorException
    {
        Exception containedException = error.getException();

        if(containedException == null)
        {
            throw new SFV3ErrorException(error);
        }

        //This is dumb but required.
        if(containedException instanceof ConnectException )
        {
            throw new SFV3ErrorException(error);
        }
        else if(containedException instanceof SFOutOfMemoryException)
        {
            throw new SFV3ErrorException(error);
        }
        else if(containedException instanceof UnsupportedEncodingException)
        {
            throw new SFV3ErrorException(error);
        }
        else if(containedException instanceof URISyntaxException)
        {
            throw new SFV3ErrorException(error);
        }
        else if(containedException instanceof UnknownHostException)
        {
            throw new SFV3ErrorException(error);
        }
        else if(containedException instanceof IOException )
        {
            throw new SFV3ErrorException(error);
        }
        else
        {
            throw new SFV3ErrorException(error);
        }
    }

	private T returnResultOrThrow(T sfobject,SFV3Error v3error) throws SFV3ErrorException
	{
		if(sfobject!=null)
		{		
			return sfobject;
		}

		/*if(mResponseListener == null)
		{
		    throwExceptionOnSeparateLines(v3error);
		}
		*/
        throwExceptionOnSeparateLines(v3error);

		return null;
	}
				
				
	/**
	 *  If an error happens during parsing the success response, 
	 *  we return the exception description + the original server response in V3Error Object
	 * @throws URISyntaxException 
	 * @throws SFV3ErrorException 
	 * @throws UnsupportedEncodingException 
	 */
	protected T callSuccessResponseParser(String responseString) throws SFV3ErrorException, URISyntaxException, UnsupportedEncodingException, SFInvalidStateException
    {
		JsonParser jsonParser = new JsonParser();
		JsonElement jsonElement =jsonParser.parse(responseString);
		T sfobject = (T)SFGsonHelper.customParse(jsonElement);
				
		switch (redirectionRequired(sfobject)) 
		{
            /*
			case SYMBOLIC_LINK:
				sfobject = executeQueryOnSymbolicLink((SFSymbolicLink)sfobject);
			break;
			*/

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