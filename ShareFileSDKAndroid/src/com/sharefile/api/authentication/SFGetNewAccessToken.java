package com.sharefile.api.authentication;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.sharefile.api.SFV3Error;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.https.SFHttpsCaller;
import com.sharefile.api.interfaces.SFGetNewAccessTokenListener;
import com.sharefile.java.log.SLog;

public class SFGetNewAccessToken implements Runnable
{
	private static final String TAG = "-SFGetNewAccessToken";
	private int mHttpErrorCode = HttpsURLConnection.HTTP_OK;
	private String mResponseString = null;
	private final SFGetNewAccessTokenListener mCallback;		
	private SFV3Error mV3Error = null;	
	private final SFOAuth2Token mOldAccessToken;
	private SFOAuth2Token mNewAccessToken = null;
	private final String mWebLoginClientID;
	private final String mWebLoginClientSecret;
		
	public SFGetNewAccessToken(SFOAuth2Token oldtoken,SFGetNewAccessTokenListener callback,String clientID,String clientSecret)
	{		
		mCallback = callback;
		mOldAccessToken = oldtoken;
		mWebLoginClientID = clientID;
		mWebLoginClientSecret = clientSecret;
	}
	
	private final String buildWebLoginTokenUrl(String controlplane,String subdomain)
	{
		String strDot = controlplane.startsWith(".")?"":".";
		
		final String str = "https://"+subdomain+strDot+controlplane+"/oauth/token";
		return str;
	}
	
	/**     
    grant_type=authorization_code&code=CvJ4LMgMDHuZGLXgJgJdDYR17Hd3b5&client_id=3fTJB2mjJ7KaNflPWJ8MylHos&client_secret=Y8LzHuYvxjxc8FE7s1HNe96s0xGVM4
	 */
	private String getBodyForWebLogin(List<NameValuePair> params) throws UnsupportedEncodingException
	{
	    StringBuilder result = new StringBuilder();
	    boolean first = true;

	    for (NameValuePair pair : params)
	    {
	        if (first)
	        {
	            first = false;
	        }
	        else
	        {
	            result.append("&"); 
	        }

	        result.append(pair.getName());
	        result.append("=");
	        result.append(pair.getValue());
	    }

	    return result.toString();
	}
	
	public static void postBody(URLConnection conn, String body) throws IOException
	{		
		OutputStream os = conn.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		writer.write(body);
		writer.flush();
		writer.close();
		os.close();
	}
	/**
	 *  Never call the function getNewAccessToken() on UI thread. 
	 *  The override callback is only for callers that explicitly invoke  getNewAccessToken() 
	 *  These could be other AsyncTasks which directly call getNewAccessToken() and don't want to execute another internal AsyncTask for this purpose.
	 *  We need such tasks to be able get the error codes correctly.		   
	 */
	public void getNewAccessToken()
	{
		
		try 
		{									
			URL url = new URL(buildWebLoginTokenUrl(mOldAccessToken.getApiCP(), mOldAccessToken.getSubdomain()));
				  						
			URLConnection conn = SFHttpsCaller.getURLConnection(url);
			SFHttpsCaller.setPostMethod(conn);
			SFHttpsCaller.setAcceptLanguage(conn);
											
			List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
			nvPairs.add(new BasicNameValuePair(SFKeywords.GRANT_TYPE, SFKeywords.REFRESH_TOKEN));
			nvPairs.add(new BasicNameValuePair(SFKeywords.REFRESH_TOKEN, mOldAccessToken.getRefreshToken()));
			nvPairs.add(new BasicNameValuePair(SFKeywords.CLIENT_ID, mWebLoginClientID));
			nvPairs.add(new BasicNameValuePair(SFKeywords.CLIENT_SECRET, mWebLoginClientSecret));		
			
			String body = getBodyForWebLogin(nvPairs);
			
			conn.setRequestProperty(SFKeywords.CONTENT_LENGTH, ""+body.length());			
			conn.setRequestProperty(SFKeywords.CONTENT_TYPE, SFKeywords.APPLICATION_FORM_URLENCODED);
			 				
			postBody(conn, body);
			
			mHttpErrorCode = SFHttpsCaller.safeGetResponseCode(conn);
																			    
			if(mHttpErrorCode == HttpsURLConnection.HTTP_OK)
			{										
				mResponseString = SFHttpsCaller.readResponse(conn);
			}		
			else
			{
				mResponseString = SFHttpsCaller.readErrorResponse(conn);
													
			}				    			
		}		
		catch (Exception e) 
		{
			mHttpErrorCode = SFSDK.INTERNAL_HTTP_ERROR;
			mResponseString = e.getLocalizedMessage();
		} 		
		
		parseResponse(mHttpErrorCode, mResponseString);
						
		callResponseListeners();
	}
		

	/**
	 *   Parse the response to the best of our ability. At the end of this function the FinalResponse object 
	 *   has to be filled with an ErrorCode or HTTP_OK and the V3Error or SFOBject should be filled based on success or failure or 
	 *   response parsing.	 
	 */
	private void parseResponse(int httpCode,String responseString)
	{
		switch(httpCode)
		{
			case HttpsURLConnection.HTTP_OK:
				callSuccessResponseParser(responseString);				
			break;	
									
			case HttpsURLConnection.HTTP_UNAUTHORIZED:
				mHttpErrorCode = HttpsURLConnection.HTTP_UNAUTHORIZED;
				mV3Error = new SFV3Error(httpCode,null,responseString);				
			break;
			
			case SFSDK.INTERNAL_HTTP_ERROR:				
				callInternalErrorResponseFiller(httpCode, responseString, null);
			break;
			
			default:
				callFailureResponseParser(httpCode, responseString);
			break;				
		}				
	}
	
	private void callSuccessResponseParser(String responseString)
	{
		try
		{
			mHttpErrorCode = HttpsURLConnection.HTTP_OK;
			mNewAccessToken = new SFOAuth2Token(responseString);
		}
		catch(Exception e)
		{			
			callInternalErrorResponseFiller(SFSDK.INTERNAL_HTTP_ERROR, e.getLocalizedMessage(), responseString);
		}
	}
	
	private void callFailureResponseParser(int httpCode, String responseString)
	{
		try
		{
			mHttpErrorCode = httpCode;
			mV3Error = new SFV3Error(httpCode,responseString,null);
		}
		catch(Exception e)
		{			
			callInternalErrorResponseFiller(SFSDK.INTERNAL_HTTP_ERROR, e.getLocalizedMessage(), responseString);
		}
	}
	
	private void callResponseListeners()
	{
		if(mCallback == null)
		{
			return;
		}
		
		try
		{
			switch(mHttpErrorCode)
			{
				case HttpsURLConnection.HTTP_OK:
					mCallback.successGetAccessToken(mNewAccessToken);				
				break;	
																
				default:
					mCallback.errorGetAccessToken(mV3Error);
				break;				
			}
		}
		catch(Exception ex)
		{
			SLog.d(TAG, "!!Exception calling the responseListener " , ex);
		}
	}
		
	/**
	 *   This is a filler only. wont do any parsing.
	 */
	private void callInternalErrorResponseFiller(int httpCode,String errorDetails,String extraInfo)
	{
		mHttpErrorCode = httpCode;
		mV3Error = new SFV3Error(httpCode,errorDetails,extraInfo);		
	}

	public Thread startNewThread()
	{
		Thread sfApithread = new Thread(this);		
		sfApithread.start();
		return sfApithread;
	}		
	
	@Override
	public void run() 
	{		
		getNewAccessToken();
	}
}