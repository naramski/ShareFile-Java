package com.sharefile.api.authentication;

import com.sharefile.api.SFSdk;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.exceptions.SFJsonException;
import com.sharefile.api.exceptions.SFNotAuthorizedException;
import com.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.sharefile.api.https.SFHttpsCaller;
import com.sharefile.api.log.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class SFOAuthService
{
	
	private static final String TAG = SFKeywords.TAG + "-simpleauth";

	/**
	 * Authenticate via username/password
	 *
	 * @param hostname
	 *            - hostname like "myaccount.sharefile.com"
	 * @param clientId
	 *            - your apiClient id
	 * @param clientSecret
	 *            - your apiClient secret
	 * @param username
	 *            - my@user.name
	 * @param password
	 *            - mypassword
	 * @return an OAuth2Token instance
	 * @throws SFJsonException
	 */
	public static SFOAuth2Token authenticate(String hostname, String clientId,String clientSecret, String username, String password)
			throws SFNotAuthorizedException, SFJsonException
	{
        HttpsURLConnection connection = null;
        try
        {
            URL grantUrl = new URL(String.format("https://%s/oauth/token", hostname));

            HashMap<String, String> params = new HashMap<String, String>();
            StringBuilder queryString = new StringBuilder();

            params.put(SFKeywords.GRANT_TYPE, SFKeywords.PASSWORD);
            params.put(SFKeywords.CLIENT_ID, clientId);
            params.put(SFKeywords.CLIENT_SECRET, clientSecret);
            params.put(SFKeywords.USERNAME, username);
            params.put(SFKeywords.PASSWORD, password);

            for (Map.Entry<String, String> entry : params.entrySet()) {
                queryString.append(String.format("%s=%s&", entry.getKey(), URLEncoder.encode(entry.getValue(), SFKeywords.UTF_8)));
            }

            queryString.deleteCharAt(queryString.length() - 1);
            //Logger.d(TAG,"%s", queryString);

            connection = (HttpsURLConnection) grantUrl.openConnection();
            connection.setRequestMethod(SFHttpMethod.POST.toString());
            connection.addRequestProperty(SFKeywords.CONTENT_TYPE, SFKeywords.APPLICATION_FORM_URLENCODED);

            connection.setDoOutput(true);
            connection.connect();

            connection.getOutputStream().write(queryString.toString().getBytes());

            switch (SFHttpsCaller.safeGetResponseCode(connection))
            {
                case HttpsURLConnection.HTTP_OK:
                    String response = SFHttpsCaller.readResponse(connection);
                    return new SFOAuth2Token(response);

                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    throw new SFNotAuthorizedException("401");

                default:
                    String errResponse = SFHttpsCaller.readErrorResponse(connection);
                    throw new SFNotAuthorizedException(errResponse);
            }

        }
        catch (IOException e)
        {
            Logger.e(TAG,e);
            throw new SFNotAuthorizedException(e);
        }
        finally
        {
            if(connection!=null)
            {
                connection.disconnect();
            }
        }
	}

    public static SFOAuth2Token authenticate(String subdomain,String topLeveDomain,String samlAssertion)
            throws SFNotAuthorizedException, SFJsonException
    {
        HttpsURLConnection conn = null;


        URL url = null;
        try
        {
            url = new URL(samlToOAuthTokenUrl(subdomain, topLeveDomain));

            Logger.v(TAG, "Get AccessToken from: " + url);
            conn = (HttpsURLConnection) url.openConnection();

            SFHttpsCaller.setMethod(conn, "POST");

            List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
            nvPairs.add(new BasicNameValuePair(SFKeywords.CLIENT_ID, SFSdk.getClientId()));
            nvPairs.add(new BasicNameValuePair(SFKeywords.CLIENT_SECRET, SFSdk.getClientSecret()));
            nvPairs.add(new BasicNameValuePair(SFKeywords.GRANT_TYPE, URLEncoder.encode("urn:ietf:params:oauth:grant-type:saml2-bearer", "UTF-8")));
            nvPairs.add(new BasicNameValuePair("samlresponse", URLEncoder.encode(samlAssertion, "UTF-8")));
            String body = SFHttpsCaller.getBodyForWebLogin(nvPairs);

            conn.setRequestProperty(SFKeywords.CONTENT_LENGTH, "" + body.length());
            conn.setRequestProperty(SFKeywords.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");

            SFHttpsCaller.postBody(conn, body);

            int httpErrorCode = SFHttpsCaller.safeGetResponseCode(conn);

            Logger.v(TAG, "httpErrorCode = " + httpErrorCode);

            switch (httpErrorCode )
            {
                case HttpsURLConnection.HTTP_OK:
                    String response = SFHttpsCaller.readResponse(conn);
                    return new SFOAuth2Token(response);
                //break;

                case 401:
                    throw new SFNotAuthorizedException("401");
                //break;

                default:
                    String error = SFHttpsCaller.readErrorResponse(conn);
                    throw new SFNotAuthorizedException(error);
                //break;
            }
        }
        catch (IOException e)
        {
            Logger.e(TAG, e);
            throw new SFNotAuthorizedException(e);
        }
        finally
        {
            if(conn!=null)
            {
                conn.disconnect();
            }
        }
    }

    private static String samlToOAuthTokenUrl(String subdomain,String topleveldomain)
    {
        String url = "https://" + subdomain + topleveldomain + SFKeywords.SF_OAUTH_TOKEN_PATH;

        return url;
    }

    public static SFOAuth2Token refreshOAuthToken(SFOAuth2Token oldToken, String clientId,String clientSecret)
            throws IOException, SFOAuthTokenRenewException
    {
        SFOAuthTokenRenewer tokenRenewer = new SFOAuthTokenRenewer(oldToken,clientId,clientSecret);
        return tokenRenewer.getNewAccessToken();
    }
}
