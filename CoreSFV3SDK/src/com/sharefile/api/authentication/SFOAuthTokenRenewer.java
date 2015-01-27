package com.sharefile.api.authentication;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.sharefile.api.SFTokenRenewError;
import com.sharefile.api.SFV3Error;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.exceptions.SFV3ErrorException;
import com.sharefile.api.https.SFHttpsCaller;
import com.sharefile.api.interfaces.SFGetNewAccessTokenListener;
import com.sharefile.java.log.SLog;

/**
 *   This provides the bare minimum functionality to renew the access token. Client app is free to use its own threading mechanism
 */
public class SFOAuthTokenRenewer
{
	private static final String TAG = SFKeywords.TAG + "-SFGetNewAccessToken";
	
	private final SFGetNewAccessTokenListener mCallback;		
	
	private final SFOAuth2Token mOldAccessToken;
	private SFOAuth2Token mNewAccessToken = null;
	private final String mWebLoginClientID;
	private final String mWebLoginClientSecret;
	private SFV3Error mSFV3Error = null;
		
	public SFOAuthTokenRenewer(SFOAuth2Token oldtoken,SFGetNewAccessTokenListener callback,String clientID,String clientSecret)
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
		
	/**
	 *  This will call in sequence:
	 *  <br>getNewAccessToken();
		<br>callResponseListeners();
	 */
	public void getNewAccessTokenEx() throws SFV3ErrorException
	{
		getNewAccessToken();
		callResponseListeners();
	}
	/**
	 *  This function offers fine grained control so that the app call getTheAccessToken and call response listeners separately.
	 *  
	 *  For Example: Android apps which can use an AsycnTask for this can call
	 *  <br> doInBackGround()
	 *  <br>{
	 *  <br>   getNewAccessToken();
	 *  <br>}	
	 *  
	 *  <br> onPostExecute()
	 *  <br>{
	 *  <br>   callResponseListener();
	 *  <br>}
	 *  
	 */
	public SFOAuth2Token getNewAccessToken() throws SFV3ErrorException
	{
        SLog.d(TAG,"Renew Token from with: [" + mOldAccessToken.getAccessToken() + "]:["+mOldAccessToken.getRefreshToken()+"]");//TODO-REMOVE-LOG

		int httpErrorCode = SFSDK.INTERNAL_HTTP_ERROR;
		String responseString;
		
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
			 				
			SFHttpsCaller.postBody(conn, body);
			
			httpErrorCode = SFHttpsCaller.safeGetResponseCode(conn);

            switch(httpErrorCode)
			{
				case HttpsURLConnection.HTTP_OK:
					responseString = SFHttpsCaller.readResponse(conn);
					mNewAccessToken = new SFOAuth2Token(responseString);
				break;	
								
				case HttpsURLConnection.HTTP_UNAUTHORIZED:
					mSFV3Error = new SFV3Error(HttpsURLConnection.HTTP_UNAUTHORIZED,null,null);
				break;
				
				default:
					responseString = SFHttpsCaller.readErrorResponse(conn);
                    SLog.d(TAG, "!!! Server err repsonse for token renew = " + responseString);
					mSFV3Error = new SFTokenRenewError(httpErrorCode,responseString,null);
				break;	
			}							    			
						
		}		
		catch (Exception e) 
		{
			mSFV3Error = new SFTokenRenewError(httpErrorCode,null,e);
		} 		
										
		
		return returnResultOrThrowException();
	}
				
	public SFV3Error getError()
	{
		return mSFV3Error;
	}
	
	/**
	 *  Throws SFV3Error as an exception only when the callers of the class have not installed callback. 
	 */
	private SFOAuth2Token returnResultOrThrowException() throws SFV3ErrorException
	{
		if(mNewAccessToken!=null)
		{
			return mNewAccessToken;
		}
		
		//throw exception on when callbacks are not installed .else the user is expect to get the response on callbacks
		if(mCallback ==null)
		{
			throw new SFV3ErrorException(mSFV3Error);
		}
				
		return null;
	}
	
	public void callResponseListeners()
	{
		if(mCallback == null)
		{
			return;
		}
		
		try
		{
			if(mSFV3Error == null)
			{
				mCallback.successGetAccessToken(mNewAccessToken);				
			}
			else
			{
				mCallback.errorGetAccessToken(mSFV3Error);								
			}
		}
		catch(Exception ex)
		{
			SLog.e(TAG, "!!Exception calling the responseListener " , ex);
		}
	}
}