package com.sharefile.api.https;

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
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;

import com.sharefile.api.SFV3Error;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.enumerations.SFProvider;
import com.sharefile.api.utils.Utils;
import com.sharefile.java.log.SLog;
import org.apache.commons.codec.binary.Base64;

public class SFHttpsCaller 
{
	private static final String TAG = SFKeywords.TAG + "-SFHttpCaller";
	
	private static final String NO_AUTH_CHALLENGES = "No authentication challenges found";
	private static final String OUT_OF_MEMORY = "memory";
	
	//private static CookieManager m_cookieManager = null;
		
	public static void setBasicAuth(URLConnection conn,String username,String password)
	{			
		String combinepass = username +SFKeywords.COLON + password;
		String basicAuth = "Basic " + new String(Base64.encodeBase64(combinepass.getBytes()));
		conn.setRequestProperty ("Authorization", basicAuth);
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
			
	public static URLConnection getURLConnection(URL url) throws IOException
	{
		//trustAll();
		return url.openConnection();
	}
		
	/**     
     grant_type=authorization_code&code=CvJ4LMgMDHuZGLXgJgJdDYR17Hd3b5&client_id=3fTJB2mjJ7KaNflPWJ8MylHos&client_secret=Y8LzHuYvxjxc8FE7s1HNe96s0xGVM4
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
		conn.setRequestProperty("Accept-Language", Utils.getAcceptLanguageString());
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
	
	public static void setMethod(URLConnection conn,String methodName) throws ProtocolException
	{
		if(methodName.equalsIgnoreCase(SFHttpMethod.POST.toString()))
		{
			setPostMethod(conn);
		}
		else if(methodName.equalsIgnoreCase(SFHttpMethod.DELETE.toString()))
		{
			setDeleteMethod(conn);
		} 
		else if(methodName.equalsIgnoreCase(SFHttpMethod.GET.toString()))
		{
			setRequestMethod(conn, methodName);
		}
	}
	
	private static void setDeleteMethod(URLConnection conn) throws ProtocolException
	{		
		setRequestMethod(conn,SFHttpMethod.DELETE.toString());		
		conn.setDoInput(true);
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
				SLog.d(TAG, "Gracefull catching out of memmory");
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
	public static synchronized int safeGetResponseCode(URLConnection conn) throws Exception
	{
		int httpErrorCode = HttpsURLConnection.HTTP_INTERNAL_ERROR;
		
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
		
		SLog.d(TAG,"ERR_CODE: " + httpErrorCode);
		
		return httpErrorCode;		
	}
	
	
	/**
	 *  if responsecode != HTTP_OK 
	 *  
	 *  <p> handle it appropriately by trying to read the error stream and constructing the 
	 *  <p> error message. Auth errors may require reading the credentials and retrying.
	 *  
	 *  if responsecode == HTTP_OK then read the cookies.
	 *  
	 *  <p>This function always returns a valid V3Error in any non-success case or NULL if HTTP_OK
	 * @throws IOException 
	 */
	public static SFV3Error handleErrorAndCookies(URLConnection conn, int httpErrorCode,URL url,SFCookieManager cookieManager) throws IOException
	{
		SFV3Error v3Error = null;
		
		if(httpErrorCode == HttpsURLConnection.HTTP_OK || httpErrorCode == HttpsURLConnection.HTTP_NO_CONTENT)
		{
			getAndStoreCookies(conn,url,cookieManager);
			return v3Error;
		}
		
		try
		{
			String inputLine = readErrorResponse(conn);
			
			SLog.d(TAG,  "ERR PAGE: " + inputLine);
			
			v3Error = new SFV3Error(httpErrorCode,inputLine);
		}
		catch (Exception e) 
		{			
			//try constructing the error from the exception.
			
			v3Error = new SFV3Error(httpErrorCode, e.getLocalizedMessage());
		}
		
		return v3Error;
		
	}
	
		
	public static synchronized void getAndStoreCookies(URLConnection conn, URL url,SFCookieManager cookieManager) throws IOException
	{
		if(cookieManager!=null)
		{						
			cookieManager.readCookiesFromConnection(conn);
		}				
	}
			
	public static String readResponse(URLConnection conn) throws IOException 
	{
		StringBuilder sb = new StringBuilder();
				
		InputStream is = conn.getInputStream();
		
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
			SLog.d(TAG, "Error: " , e);
			
			throw new IOException("Out of memory");
		}
		
		urlstream.close();
		
		String response = sb.toString();
				
		SLog.d(TAG, "SUCCESS RESPONSE size: " + response.length());						
			
		return response;
	}
			
	public static String readErrorResponse(URLConnection conn) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		
		BufferedReader urlstream = null;
		
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
		
		SLog.d(TAG, "ERROR RESPONSE SIZE: " + sb.length());
		
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
	 * TODO: This needs a major revamp. We need User specific cookies to be set and CIFS/SharePoint specific authentication to be handled
	   We need a separate auth manager here to handle the setting of correct auth header based on the provider type and well as the user.
	 * @throws IOException 
	*/	
	public static void addAuthenticationHeader(URLConnection connection,SFOAuth2Token token,String userName,String password, SFCookieManager cookieManager) throws IOException
	{
		String path = connection.getURL().getPath();
		
		if(cookieManager!=null)
		{			
			cookieManager.setCookies(connection);
		}
		
		switch(SFProvider.getProviderTypeFromString(path))
		{
			case PROVIDER_TYPE_SF:
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
