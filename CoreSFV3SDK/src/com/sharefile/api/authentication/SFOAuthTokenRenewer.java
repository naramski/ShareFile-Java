package com.sharefile.api.authentication;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.sharefile.api.gson.SFGsonHelper;
import com.sharefile.api.https.SFHttpsCaller;
import com.sharefile.api.log.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 *   This provides the bare minimum functionality to renew the access token. Client app is free to use its own threading mechanism
 */
public class SFOAuthTokenRenewer
{
	private static final String TAG = SFKeywords.TAG + "SFOAuthTokenRenewer";

	private final SFOAuth2Token mOldAccessToken;
	private final String mWebLoginClientID;
	private final String mWebLoginClientSecret;

	public SFOAuthTokenRenewer(SFOAuth2Token oldtoken,String clientID,String clientSecret)
	{
		mOldAccessToken = oldtoken;
		mWebLoginClientID = clientID;
		mWebLoginClientSecret = clientSecret;
	}
	
	private final String buildWebLoginTokenUrl(String controlplane,String subdomain)
	{
		String strDot = controlplane.startsWith(".")?"":".";
		
		return  "https://"+subdomain+strDot+controlplane+"/oauth/token";
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

    private String parseError(String serverRespString,int serverHttpCode)
    {
        if(serverRespString == null)
        {
                return "Unknown Error.("+ serverHttpCode+")";
        }

        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(serverRespString);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        //code = SFGsonHelper.getString(jsonObject, "error", "");
        return SFGsonHelper.getString(jsonObject, "error_description", "");
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
	public SFOAuth2Token getNewAccessToken() throws SFOAuthTokenRenewException
	{
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
					return new SFOAuth2Token(responseString);
				//break;
								
				case HttpsURLConnection.HTTP_UNAUTHORIZED:
					throw new SFOAuthTokenRenewException("UnAuthorized(401");
				//break;
				
				default:
					responseString = SFHttpsCaller.readErrorResponse(conn);
                    Logger.d(TAG, "!!! Server err repsonse for token renew = " + responseString);
					throw new SFOAuthTokenRenewException(parseError(responseString,httpErrorCode));
				//break;
			}							    			
						
		}		
		catch (Exception e) 
		{
			throw new SFOAuthTokenRenewException(e);
		}
	}
}