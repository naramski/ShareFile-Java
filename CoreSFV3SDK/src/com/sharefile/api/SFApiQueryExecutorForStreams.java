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
import com.sharefile.api.interfaces.SFApiStreamResponse;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFRedirection;
import com.sharefile.api.models.SFSymbolicLink;
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
class SFApiQueryExecutorForStreams implements ISFApiExecuteQuery
{
	private static final String TAG = SFKeywords.TAG + "-SFApiThread";

	private final ISFQuery<InputStream> mQuery;
	private final SFApiStreamResponse mResponseListener;
	private final SFCookieManager mCookieManager;
	private final SFConfiguration mAppSpecificConfig;
    private final SFApiClient mSFApiClient;

	private final class Response
	{
		InputStream returnObject;
		SFV3Error errorObject;

		public void setResponse(InputStream ret,SFV3Error err)
		{
			returnObject = ret;
			errorObject = err;
		}
	}

	private Response mResponse = null;

	public SFApiQueryExecutorForStreams(SFApiClient apiClient, ISFQuery<InputStream> query,
                                        SFApiStreamResponse responseListener,
                                        SFCookieManager cookieManager,
                                        SFConfiguration config
    ) throws SFInvalidStateException
	{
		mSFApiClient = apiClient;
		mQuery = query;
		mResponseListener = responseListener;
		mCookieManager = cookieManager;
		mAppSpecificConfig = config;
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

	@Override
	public InputStream executeBlockingQuery() throws SFV3ErrorException, SFInvalidStateException
    {
        mSFApiClient.validateClientState();

        mResponse = new Response();

        int httpErrorCode = SFSDK.INTERNAL_HTTP_ERROR;
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

            SFHttpsCaller.addAuthenticationHeader(connection, mSFApiClient.getOAuthToken(), mQuery.getUserName(), mQuery.getPassword(), mCookieManager);

            handleHttPost(connection);

            SLog.d(TAG, mQuery.getHttpMethod() + " " + urlstr);

            connection.connect();

            httpErrorCode = SFHttpsCaller.safeGetResponseCode(connection);

            SFHttpsCaller.getAndStoreCookies(connection, url, mCookieManager);

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
                }
            }

            mResponse.setResponse(connection.getInputStream(),null);
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
            SFV3Error sfV3error = new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR, null, new SFOutOfMemoryException(Arrays.toString(e.getStackTrace())));
            mResponse.setResponse(null, sfV3error);
        }

		return returnResultOrThrow(mResponse.returnObject,mResponse.errorObject);
	}

	@SuppressWarnings("unchecked")
	protected void callResponseListeners(InputStream inputStream,SFV3Error v3error)
	{
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
				mResponseListener.sfApiSuccess(inputStream);
			}
		}
		catch(Exception ex)
		{
			SLog.e(TAG,ex);
		}
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

	private InputStream returnResultOrThrow(InputStream inputStream,SFV3Error v3error) throws SFV3ErrorException
	{
		if(inputStream!=null)
		{
			return inputStream;
		}

		if(mResponseListener == null)
		{
			throwExceptionOnSeparateLines(v3error);
		}

		return null;
	}
}