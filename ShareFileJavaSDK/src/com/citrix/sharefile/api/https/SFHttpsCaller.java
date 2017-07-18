package com.citrix.sharefile.api.https;


import com.citrix.sharefile.api.SFConnectionManager;
import com.citrix.sharefile.api.authentication.SFOAuth2Token;
import com.citrix.sharefile.api.constants.SFKeywords;
import com.citrix.sharefile.api.enumerations.SFHttpMethod;
import com.citrix.sharefile.api.SFProvider;
import com.citrix.sharefile.api.exceptions.SFFormsAuthenticationCookies;
import com.citrix.sharefile.api.utils.Utils;
import com.citrix.sharefile.api.log.Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class SFHttpsCaller 
{
	private static final String TAG = SFKeywords.TAG + "-SFHttpCaller";
	
	private static final String NO_AUTH_CHALLENGES = "No authentication challenges found";
	private static final String OUT_OF_MEMORY = "memory";
	private static final String ROOT_PARAM = "root";
	private static final String REDIRECT_URL_PARAM = "redirect_url";
	
	//private static CookieManager m_cookieManager = null;
		
	public static void setBasicAuth(URLConnection conn,String username,String password)
	{			
		String combinepass = username +SFKeywords.COLON + password;
		String basicAuth = "Basic " + new String(Base64.encodeBase64(combinepass.getBytes()));
		conn.setRequestProperty("Authorization", basicAuth);
	}
	
	public static void postBody(URLConnection conn, String body) throws IOException
	{		
		OutputStream os = conn.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, SFKeywords.UTF_8));
		writer.write(body);
		writer.flush();
		writer.close();
		os.close();
	}

	/**     
     grant_type=authorization_code&code=CvJ4LMgMDHuZGLXgJgJdDYR17Hd3b5&client_id=xyz&client_secret=abc
	 */
	public static String getBodyForWebLogin(List<NameValuePair> params) throws UnsupportedEncodingException
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
	            result.append(SFKeywords.CHAR_AMPERSAND); 
	        }

	        result.append(pair.getName());
	        result.append(SFKeywords.EQUALS);
	        result.append(pair.getValue());
	    }

	    return result.toString();
	}
	
	public static void setAcceptLanguage(URLConnection conn)
	{
		conn.setRequestProperty(SFKeywords.ACCEPT_LANGUAGE, Utils.getAcceptLanguageString());
	}
	
	private static void setRequestMethod(URLConnection conn, String method) throws ProtocolException
	{
		if(conn instanceof HttpsURLConnection)
		{
			((HttpsURLConnection) conn).setRequestMethod(method);
		}
		else if(conn instanceof HttpURLConnection)
		{
			((HttpURLConnection) conn).setRequestMethod(method);
		} 
	}
	
	public static void setPostMethod(URLConnection conn) throws ProtocolException
	{
		setRequestMethod(conn,SFHttpMethod.POST.toString());
		conn.setDoInput(true);
		conn.setDoOutput(true);
	}
	
	/**
	 * PATCH is not recognized by android. Use POST as surrogate
	 */
	private static final boolean overridePatchMethod(URLConnection conn,String methodName)
	{	
		boolean ret = false;
		
		if(methodName.equalsIgnoreCase(SFHttpMethod.PATCH.toString()))
		{			
			conn.setRequestProperty(SFKeywords.HTTP_METHOD_OVERRIDE, SFHttpMethod.PATCH.toString());
			
			ret = true;
		}
		
		return ret;
	}
	
	/**
	 * DELETE does not support setOutputTrue on Android. Use POST as surrogate instead on all systems.
	 */
	private static final boolean overrideDeleteMethod(URLConnection conn,String methodName, String optionalBody)
	{
		boolean ret = false;

		//Only use POST surrogates for DELETE which have non-empty BODY
		if(methodName.equalsIgnoreCase(SFHttpMethod.DELETE.toString()) && !Utils.isEmpty(optionalBody))
		{
			conn.setRequestProperty(SFKeywords.HTTP_METHOD_OVERRIDE, SFHttpMethod.DELETE.toString());

			ret = true;
		}

		return ret;
	}

	public static void setMethod(URLConnection conn,String methodName, String optionalBody) throws ProtocolException
	{		
		if(overridePatchMethod(conn, methodName))
		{
			methodName = SFHttpMethod.POST.toString();
		}

		if(overrideDeleteMethod(conn, methodName,optionalBody))
		{
			methodName = SFHttpMethod.POST.toString();
		}

		setRequestMethod(conn, methodName);
				 
		if(methodName.equalsIgnoreCase(SFHttpMethod.GET.toString()) ||
		   methodName.equalsIgnoreCase(SFHttpMethod.DELETE.toString()))
		{
			return;
		}
		
		conn.setDoInput(true);
		conn.setDoOutput(true); //POST, PUT, DELETE
	}
	
	public static int catchIfAuthException(IOException e) throws IOException
	{
		String errMessage = e.getLocalizedMessage();
		
		if(errMessage!=null)
		{
			if(errMessage.contains(NO_AUTH_CHALLENGES))
			{
				return HttpsURLConnection.HTTP_UNAUTHORIZED;
			}	
			else
			{
				throw e;
			}
		}
		else
		{
			throw e;
		}
		
		//return 0;
	}
	
	public static int catchIfOutOfMemoryException(Exception e,int origcode) 
	{
		String errMessage = e.getLocalizedMessage();
		
		if(errMessage!=null)
		{
			if(errMessage.contains(OUT_OF_MEMORY))
			{				
				Logger.d(TAG, "Gracefull catching out of memmory");
				return 500;
			}				
		}		
		
		return origcode;
	}
	
	/**
	 * The http functions sometimes respond with 401 error or sometimes throw and exception
	 * <p> depending on what the server returns. So we need a generic way to get the error code.
	 * @throws IOException 
	 * 
	 */
	public static int safeGetResponseCode(URLConnection conn) throws IOException
	{
		int httpErrorCode;
		
		try
		{			
			if(conn instanceof HttpsURLConnection)
			{
				httpErrorCode = ((HttpsURLConnection) conn).getResponseCode();
			}
			else
			{
				httpErrorCode = ((HttpURLConnection) conn).getResponseCode();
			}
		}
		catch (IOException e) //on wrong creds this throws exeption 
		{
			httpErrorCode = catchIfAuthException(e);
		}
		
		Logger.d(TAG,"ERR_CODE: " + httpErrorCode);
		
		return httpErrorCode;		
	}


    public static void getAndStoreCookies(URLConnection conn, URL url,SFCookieManager cookieManager) throws IOException
	{
		if(cookieManager!=null)
		{						
			cookieManager.readCookiesFromConnection(conn);
		}				
	}

	public static SFFormsAuthenticationCookies getFormsAuthResponseCookies(URL url, URLConnection connection, SFCookieManager cookieManager) throws IOException
	{
		if(cookieManager != null) {
			SFFormsAuthenticationCookies sfFormsAuthenticationCookies =  cookieManager.readFormsAuthCookies(connection);
			if(sfFormsAuthenticationCookies != null) {
				String rootParam = getRootQueryParamter(url);
				//Adding the root parameter to the login and the token url
				String tokenURL = getTokenURL(sfFormsAuthenticationCookies.getToken(), rootParam);
				String loginURL = getLoginURL(sfFormsAuthenticationCookies.getLoginURL(), tokenURL, rootParam);

				sfFormsAuthenticationCookies.setLoginURL(loginURL);
				sfFormsAuthenticationCookies.setToken(tokenURL);
				return sfFormsAuthenticationCookies;
			}
		}
		return null;
	}

	private static String getTokenURL(String token, String root)
	{
		try {
			return addQueryParams(token, ROOT_PARAM, root);
		}
		catch (URISyntaxException ex) {
			Logger.e(TAG, "error: ", ex);
			return null;
		}
	}

	private static String getLoginURL(String login, String token, String root) {
		try {
			String loginURL = addQueryParams(login, ROOT_PARAM, root);
			return addQueryParams(loginURL, REDIRECT_URL_PARAM, token);
		}
		catch (URISyntaxException ex) {
			Logger.e(TAG, "error: ", ex);
			return null;
		}
	}

	private static String addQueryParams(String url, String name, String value) throws URISyntaxException {
		URI oldUri = new URI(url);
		String appendQuery = name + "=" + value;
		String newQuery = oldUri.getQuery();
		if (newQuery == null) {
			newQuery = appendQuery;
		} else {
			newQuery += "&" + appendQuery;
		}

		URI newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(),
				oldUri.getPath(), newQuery, oldUri.getFragment());
		return newUri.toString();
	}

	private static String getRootQueryParamter(URL url) {
		String[] pairs = url.toString().split("&");

		//Root is the first query parameter
		int idx = pairs[0].indexOf("=");
		return URLDecoder.decode(pairs[0].substring(idx + 1));
	}
			
	public static String readResponse(URLConnection conn) throws IOException 
	{
		StringBuilder sb = new StringBuilder();
				
		InputStream is = SFConnectionManager.getInputStream(conn);
		
		BufferedReader urlstream = new BufferedReader(new InputStreamReader(is));
		String inputLine;
		
		try
		{
			while ((inputLine = urlstream.readLine()) != null)
			{
				sb.append(inputLine);				
			}
		}
		catch (OutOfMemoryError e) 
		{
			Logger.d(TAG, "Error: " , e);
			
			throw new IOException("Out of memory");
		}
		
		urlstream.close();
		
		String response = sb.toString();
				
		Logger.d(TAG, "SUCCESS RESPONSE size: " + response.length());
			
		return response;
	}
			
	public static String readErrorResponse(URLConnection conn) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		
		BufferedReader urlstream;
		
		//type cast correctly.
		if(conn instanceof HttpsURLConnection)
		{
			urlstream = new BufferedReader(new InputStreamReader(((HttpsURLConnection) conn).getErrorStream()));
		}
		else if(conn instanceof HttpURLConnection)
		{
			urlstream = new BufferedReader(new InputStreamReader(((HttpURLConnection) conn).getErrorStream()));
		}
		else
		{
			return "";
		}
		
		String inputLine;
		
		while ((inputLine = urlstream.readLine()) != null)
		{
			sb.append(inputLine);
		}
		
		Logger.d(TAG, "ERROR RESPONSE SIZE: " + sb.length());
		
		urlstream.close();
				
		return sb.toString();
	}
	
	public static void disconnect(URLConnection conn)
	{
		if(conn!=null)
		{
			if(conn instanceof HttpsURLConnection)
			{
				((HttpsURLConnection) conn).disconnect();
			}
			else if(conn instanceof HttpURLConnection)
			{
				((HttpURLConnection) conn).disconnect();
			}
				
		}
	}
		
	public static void addBearerAuthorizationHeader(URLConnection connection,SFOAuth2Token token) 
	{				
		connection.addRequestProperty("Authorization",String.format("Bearer %s", token.getAccessToken()));
	}		
	
	/**
	   We need a separate auth manager here to handle the setting of correct auth header based on the provider type and well as the user.
	 * @throws IOException 
	*/	
	public static void addAuthenticationHeader(URLConnection connection,SFOAuth2Token token,String userName,String password, SFCookieManager cookieManager) throws IOException
	{
		if(cookieManager!=null)
		{			
			cookieManager.setCookies(connection);
		}

        switch(SFProvider.getProviderType(connection.getURL()))
		{
			case SFProvider.PROVIDER_TYPE_SF:
				SFHttpsCaller.addBearerAuthorizationHeader(connection, token);
			    break;
			
			default:
				if(userName!=null && password!=null)
				{			
					setBasicAuth(connection, userName, password);			
				}
			    break;
		}		
	}
}
