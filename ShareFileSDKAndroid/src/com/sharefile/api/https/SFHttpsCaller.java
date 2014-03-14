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
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;

import android.util.Base64;
import android.webkit.CookieManager;

import com.sharefile.api.SFApiQuery;
import com.sharefile.api.V3Error;
import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.android.utils.Utils;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.models.SFODataObject;

public class SFHttpsCaller 
{
	private static final String TAG = "-SFHttpCaller";
	
	private static final String NO_AUTH_CHALLENGES = "No authentication challenges found";
	private static final String UN_REACHABLE       = "resolved";
	private static final String OUT_OF_MEMORY = "memory";
	
	private static CookieManager m_cookieManager = null;
	
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
		
	private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
	{
	    StringBuilder result = new StringBuilder();
	    boolean first = true;

	    for (NameValuePair pair : params)
	    {
	        if (first)
	            first = false;
	        else
	            result.append(","); //CHECK THIS. DOES THIS NEED URLENCODE?

	        result.append("\""+pair.getName()+"\"");
	        result.append(":");
	        result.append("\""+pair.getValue()+"\"");
	    }

	    return "{" + result.toString() + "}";
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

	private static final boolean debugCookies = true;
	
	public static synchronized void setAuth(URLConnection conn, URL url,String basicAuthCreds)
	{			
		if(m_cookieManager==null)
		{			
			m_cookieManager = CookieManager.getInstance();			
		}
		
		if(basicAuthCreds!=null)
		{			
			String basicAuth = "Basic " + new String(Base64.encode(basicAuthCreds.getBytes(),Base64.NO_WRAP ));			
			conn.setRequestProperty ("Authorization", basicAuth);			
		}
		
		String cookie = m_cookieManager.getCookie(url.toString());
		if (cookie != null)
		{				
			conn.setRequestProperty("Cookie", cookie);			
		}				
	}
	
	/**
	 * Set auth cookie as well as Auth header
	 */
	public static synchronized void setExtraAuth(URLConnection conn, URL url,String basicAuthCreds)
	{			
		if(m_cookieManager==null)
		{			
			m_cookieManager = CookieManager.getInstance();			
		}
		
		if(basicAuthCreds!=null)
		{			
			String basicAuth = "Basic " + new String(Base64.encode(basicAuthCreds.getBytes(),Base64.NO_WRAP ));			
			conn.setRequestProperty ("Authorization", basicAuth);			
		}
						
		String cookie = m_cookieManager.getCookie(url.toString());
				
		conn.setRequestProperty("Cookie", cookie);		
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
				SFLog.d2(TAG, "Gracefull catching out of memmory");
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
		
		SFLog.d2(TAG,"ERR_CODE: " + httpErrorCode);
		
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
	 */
	public static V3Error handleErrorAndCookies(URLConnection conn, int httpErrorCode,URL url)
	{
		V3Error v3Error = null;
		
		if(httpErrorCode == HttpsURLConnection.HTTP_OK || httpErrorCode == HttpsURLConnection.HTTP_NO_CONTENT)
		{
			getAndStoreCookies(conn,url);
			return v3Error;
		}
		
		try
		{
			String inputLine = readErrorResponse(conn);
			
			SFLog.d2(TAG,  "ERR PAGE: " + inputLine);
			
			v3Error = new V3Error(httpErrorCode,inputLine);
		}
		catch (Exception e) 
		{			
			//try constructing the error from the exception.
			
			v3Error = new V3Error(httpErrorCode, e.getLocalizedMessage());
		}
		
		return v3Error;
		
	}
	
		
	private static void dumpHeaders(Map<String, List<String>> headerfield)
	{
		SFLog.d2(TAG, "START----------Dumping Header Feilds: " + headerfield);
		
		if(headerfield!=null)
		{
			Set<String>  keys = headerfield.keySet();
						
			if(keys!=null)
			{
				for(String key:keys)
				{
					SFLog.d2(TAG, "--- KEY:  " + key);
					
					List<String> cookie_values = headerfield.get(key);
															
					if(cookie_values!=null)
					{
						for(String s:cookie_values)
						{
							SFLog.d2(TAG,"---------Value: " + s);							    			    								
						}
					}
				}
			}
		}
		
		SFLog.d2(TAG, "!!!!!!Dumping Header Feilds:----END ");
	}
	
	private static synchronized void getAndStoreCookies(URLConnection conn, URL url)
	{
		if(m_cookieManager==null)
		{			
			m_cookieManager = CookieManager.getInstance();			
		}
		
		Map<String, List<String>> headerfield = conn.getHeaderFields();
				
		dumpHeaders(headerfield);
		
		List<String> cookie_values = headerfield.get("Set-Cookie");
		
		if(cookie_values!=null)
		{
			for(String s:cookie_values)
			{
				if(m_cookieManager!=null)
				{
					m_cookieManager.setCookie(url.toString(), s);										
				}    			    								
			}
		}
		
		SFLog.d2(TAG, "Final Stored Auth Cookie : " + m_cookieManager.getCookie(url.toString()));
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
			SFLog.d2(TAG, "Error: " + e.getLocalizedMessage());
			
			throw new IOException("Out of memory");
		}
		
		String response = sb.toString();
		
		System.out.println("SFSDK SUCCESS RESPONSE: %s"+ response);
		//SFLog.d2(TAG, "SUCCESS RESPONSE: %s", response);
		
		urlstream.close();
			
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
		
		SFLog.d2(TAG, "ERROR RESPONSE: %s",sb.toString());
		
		urlstream.close();
				
		return sb.toString();
	}
	
	/**
	 * the url should begin with https or http. The external address in case of pactera during the connector creation 
	 * does not start with https
	 */
	private static String makeValidHttpsLink(String urlstr)
	{		
		if(urlstr.startsWith("http://") || urlstr.startsWith("https://"))
		{
			return urlstr;
		}
		
		SFLog.d2(TAG, "makeValidHttpsLink =  https://" + urlstr);
		
		return "https://"+ urlstr;		
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
}
