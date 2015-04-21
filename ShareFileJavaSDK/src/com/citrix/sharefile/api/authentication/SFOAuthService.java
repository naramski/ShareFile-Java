package com.citrix.sharefile.api.authentication;

import com.citrix.sharefile.api.SFSdk;
import com.citrix.sharefile.api.constants.SFKeywords;
import com.citrix.sharefile.api.enumerations.SFHttpMethod;
import com.citrix.sharefile.api.exceptions.SFInvalidStateException;
import com.citrix.sharefile.api.exceptions.SFJsonException;
import com.citrix.sharefile.api.exceptions.SFNotAuthorizedException;
import com.citrix.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.citrix.sharefile.api.exceptions.SFSDKException;
import com.citrix.sharefile.api.https.SFHttpsCaller;
import com.citrix.sharefile.api.interfaces.IOAuthTokenCallback;
import com.citrix.sharefile.api.interfaces.ISFOAuthService;
import com.citrix.sharefile.api.log.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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

            connection = (HttpsURLConnection) grantUrl.openConnection();
            connection.setRequestMethod(SFHttpMethod.POST.toString());
            connection.setRequestProperty(SFKeywords.CONTENT_LENGTH, "" + body.length());
            connection.addRequestProperty(SFKeywords.CONTENT_TYPE, SFKeywords.APPLICATION_FORM_URLENCODED);

            connection.setDoOutput(true);
            connection.connect();

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

            SFHttpsCaller.setMethod(conn, "POST");

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
        return authenticate(subDomain,apiControlPlane,SFSdk.getClientId(),SFSdk.getClientSecret());
    }

    @Override
    public SFOAuth2Token refreshOAuthToken(SFOAuth2Token oldToken)
            throws SFOAuthTokenRenewException, SFInvalidStateException
    {
        SFSdk.validateInit();

        refreshOAuthToken(oldToken,SFSdk.getClientId(),SFSdk.getClientSecret());
        return null;
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
}