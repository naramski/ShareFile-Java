package com.citrix.sharefile.api.authentication;

import com.citrix.sharefile.api.SFConnectionManager;
import com.citrix.sharefile.api.SFSdk;
import com.citrix.sharefile.api.constants.SFKeywords;
import com.citrix.sharefile.api.enumerations.SFHttpMethod;
import com.citrix.sharefile.api.exceptions.SFInvalidStateException;
import com.citrix.sharefile.api.exceptions.SFJsonException;
import com.citrix.sharefile.api.exceptions.SFNotAuthorizedException;
import com.citrix.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.citrix.sharefile.api.exceptions.SFOtherException;
import com.citrix.sharefile.api.exceptions.SFSDKException;
import com.citrix.sharefile.api.exceptions.SFServerException;
import com.citrix.sharefile.api.gson.SFGsonHelper;
import com.citrix.sharefile.api.https.SFHttpsCaller;
import com.citrix.sharefile.api.interfaces.IOAuthTokenCallback;
import com.citrix.sharefile.api.interfaces.ISFOAuthService;
import com.citrix.sharefile.api.log.Logger;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class SFOAuthService implements ISFOAuthService
{

	private static final String TAG = SFKeywords.TAG + "-simpleauth";

	/**
	 * Authenticate via username/password
	 *
	 * @param subDomain
	 *            - hostname like "yourcompanyname"
     * @param apiControlPlane
     *            - hostname like "sharefile.com"
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
	protected SFOAuth2Token authenticate(String subDomain, String apiControlPlane, String clientId,String clientSecret, String username, String password)
			throws SFNotAuthorizedException, SFJsonException
	{
        HttpsURLConnection connection = null;
        try
        {
            URL grantUrl = new URL(oAuthTokenUrl(subDomain,apiControlPlane));

            List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
            nvPairs.add(new BasicNameValuePair(SFKeywords.CLIENT_ID, clientId));
            nvPairs.add(new BasicNameValuePair(SFKeywords.CLIENT_SECRET, clientSecret));
            nvPairs.add(new BasicNameValuePair(SFKeywords.GRANT_TYPE, SFKeywords.PASSWORD));
            nvPairs.add(new BasicNameValuePair(SFKeywords.USERNAME,username));
            nvPairs.add(new BasicNameValuePair(SFKeywords.PASSWORD,password));
            String body = SFHttpsCaller.getBodyForWebLogin(nvPairs);

            connection = (HttpsURLConnection)SFConnectionManager.openConnection(grantUrl);
            connection.setRequestMethod(SFHttpMethod.POST.toString());
            connection.setRequestProperty(SFKeywords.CONTENT_LENGTH, "" + body.length());
            connection.addRequestProperty(SFKeywords.CONTENT_TYPE, SFKeywords.APPLICATION_FORM_URLENCODED);

            connection.setDoOutput(true);
            SFConnectionManager.connect(connection);

            SFHttpsCaller.postBody(connection,body);

            switch (SFHttpsCaller.safeGetResponseCode(connection))
            {
                case HttpsURLConnection.HTTP_OK:
                    String response = SFHttpsCaller.readResponse(connection);
                    return new SFOAuth2Token(response);

                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    throw new SFNotAuthorizedException(SFKeywords.UN_AUTHORIZED);

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

    /**
     * Authenticate via samlAssertion
     *
     * @param subDomain
     *            - hostname like "yourcompanyname"
     * @param apiControlPlane
     *            - hostname like "sharefile.com"
     * @param samlAssertion
     *            - Base64 URL encoded SAML assertion.
     * @return an OAuth2Token instance
     * @throws SFJsonException
     */
    protected SFOAuth2Token authenticate(String subDomain,String apiControlPlane, String clientId, String clientSecret,String samlAssertion)
            throws SFNotAuthorizedException, SFJsonException
    {
        HttpsURLConnection conn = null;


        URL url = null;
        try
        {
            url = new URL(oAuthTokenUrl(subDomain, apiControlPlane));

            Logger.v(TAG, "Get AccessToken from: " + url);
            conn = (HttpsURLConnection) url.openConnection();

            SFHttpsCaller.setMethod(conn, "POST",null);

            List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
            nvPairs.add(new BasicNameValuePair(SFKeywords.CLIENT_ID, clientId));
            nvPairs.add(new BasicNameValuePair(SFKeywords.CLIENT_SECRET, clientSecret));
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

                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    throw new SFNotAuthorizedException(SFKeywords.UN_AUTHORIZED);
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

    private static String oAuthTokenUrl(String subDomain, String apiControlPlane)
    {
        String strDot = "";

        if(apiControlPlane.charAt(0)!='.')
        {
            strDot = ".";
        }

        String url = "https://" + subDomain + strDot + apiControlPlane + SFKeywords.SF_OAUTH_TOKEN_PATH;

        return url;
    }

    protected SFOAuth2Token refreshOAuthToken(SFOAuth2Token oldToken, String clientId,String clientSecret)
            throws SFOAuthTokenRenewException
    {
        SFOAuthTokenRenewer tokenRenewer = new SFOAuthTokenRenewer(oldToken,clientId,clientSecret);
        return tokenRenewer.getNewAccessToken();
    }

    @Override
    public SFOAuth2Token authenticate(String subDomain,
                                      String apiControlPlane,
                                      String username,
                                      String password)
            throws SFNotAuthorizedException, SFJsonException, SFInvalidStateException
    {

        SFSdk.validateInit();

        return authenticate(subDomain,apiControlPlane,
                SFSdk.getClientId(),
                SFSdk.getClientSecret(),username,password);
    }

    @Override
    public SFOAuth2Token authenticate(String subDomain,
                                      String apiControlPlane,
                                      String samlAssertion)
            throws SFNotAuthorizedException,SFJsonException, SFInvalidStateException
    {
        SFSdk.validateInit();
        return authenticate(subDomain,apiControlPlane,
                SFSdk.getClientId(),SFSdk.getClientSecret(),samlAssertion);
    }

    @Override
    public SFOAuth2Token refreshOAuthToken(SFOAuth2Token oldToken)
            throws SFOAuthTokenRenewException, SFInvalidStateException
    {
        SFSdk.validateInit();

        return refreshOAuthToken(oldToken,SFSdk.getClientId(),SFSdk.getClientSecret());
    }

    @Override
    public void authenticateAsync(final String subDomain,
                                   final String apiControlPlane,
                                   final String username,
                                   final String password, final IOAuthTokenCallback callback)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    SFOAuth2Token token = authenticate(subDomain,apiControlPlane,username,password);
                    callback.onSuccess(token);
                }
                catch (SFSDKException e)
                {
                    callback.onError(e);
                }
            }
        });

        thread.start();
    }

    @Override
    public void authenticateAsync(final String subDomain,
                                  final String apiControlPlane,
                                  final String samlAssertion,
                                  final IOAuthTokenCallback callback)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    SFOAuth2Token token = authenticate(subDomain,apiControlPlane,samlAssertion);
                    callback.onSuccess(token);
                }
                catch (SFSDKException e)
                {
                    callback.onError(e);
                }
            }
        });

        thread.start();
    }

    @Override
    public void refreshOAuthTokenAsync(final SFOAuth2Token oldToken,
                                       final IOAuthTokenCallback callback)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    SFOAuth2Token token = refreshOAuthToken(oldToken);
                    callback.onSuccess(token);
                }
                catch (SFSDKException e)
                {
                    callback.onError(e);
                }
            }
        });

        thread.start();
    }

    /**
     *  This function converts the SFWebAuthCode obtained from the webpop
     *  and returns the OAuthToken from the server for that code.
     *
         The clientIDSecret is optional. Yf you don't pass these, the function will try to pick it up from
         those which you set during the SFSdk.init()
     */
    @Override
    public SFOAuth2Token getOAuthToken(SFWebAuthCode webAuthCode,String... clientIdSecret) throws SFServerException, SFOtherException
    {
        int httpErrorCode;
        URLConnection conn = null;
        String clientId = SFSdk.getClientId();
        String clientSecret = SFSdk.getClientSecret();

        if(clientIdSecret != null )
        {
            switch (clientIdSecret.length)
            {
                case 2:
                {
                    clientId = clientIdSecret[0];
                    clientSecret = clientIdSecret[1];
                }
                break;

                case 0:
                    //do nothing
                break;

                default:
                    throw new SFOtherException("You need to pass clientId/Secret ot nothing at all.\n In such case make sure to set the clientID/Secret from the SFSdk.init()");
            }
        }

        try {
            String urlSpec = SFAuthUtils.buildWebLoginTokenUrl(webAuthCode.mApiCp, webAuthCode.mSubDomain);
            Logger.v(TAG, "GetOauthAuthAccessToken : " + urlSpec);
            URL url = new URL(urlSpec);

            conn = SFConnectionManager.openConnection(url);
            SFHttpsCaller.setPostMethod(conn);
            SFHttpsCaller.setAcceptLanguage(conn);

            List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
            nvPairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
            nvPairs.add(new BasicNameValuePair("code", webAuthCode.mCode));
            nvPairs.add(new BasicNameValuePair("client_id", clientId));
            nvPairs.add(new BasicNameValuePair("client_secret", clientSecret));

            String body = SFAuthUtils.getBodyForWebLogin(nvPairs);

            Logger.v(TAG, "POST BODY: " + body);

            conn.setRequestProperty("Content-Length", "" + body.length());
            conn.setRequestProperty("Content-Type", "" + "application/x-www-form-urlencoded");

            SFHttpsCaller.postBody(conn, body);

            httpErrorCode = SFHttpsCaller.safeGetResponseCode(conn);

            if (httpErrorCode == HttpsURLConnection.HTTP_OK) {
                String response = SFHttpsCaller.readResponse(conn);

                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = jsonParser.parse(response).getAsJsonObject();

                String error = SFGsonHelper.getString(jsonObject, "error", "");
                if (error.length() != 0) {
                    String errorMessage = SFGsonHelper.getString(jsonObject, "errorMessage", "<unknown error>");
                    throw new SFServerException(httpErrorCode, errorMessage);
                }

                return new SFOAuth2Token(jsonObject);
            }
        }
        catch (SFServerException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new SFOtherException(e);
        }
        finally
        {
            SFHttpsCaller.disconnect(conn);
        }

        return  null;
    }
}