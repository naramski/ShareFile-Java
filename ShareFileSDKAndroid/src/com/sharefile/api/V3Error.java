package com.sharefile.api;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import com.sharefile.api.android.utils.SFLog;

import android.util.Log;

/*
 *   
  {
  "code": "InternalServerError",
  "message": 
  {
    "lang": "en-US",
    "value": "The process cannot access the file '\\\\sf_fileserver2\\nilesh\\todelete.docx' because it is being used by another process."
  }
  
  {
   "code":"NotFound",
   "message":
  	{
  	 "lang":"en-US",
  	 "value":"The item that you requested could not be found in the system."
  	}
  }
}
 */
public class V3Error 
{
	private static final String ERR_FORBIDDEN =   "Forbidden (403)";
	private static final String ERR_UNAUTHORIZD = "Unauthorized (401)";
	private static final String ERR_NOTREACHABLE = "Server Not reachable (503)";
	private static final String ERR_BADMETHOD = "Method not allowed (405)";
	
	public int httpResponseCode = 0;
	public String code = "";	
	//public String messageValue = "";
	
	public static class ErrorMessage
	{
		public String lang = null;
		public String value = null;
	}
	
	public ErrorMessage message;
			
	
	private String getErrorMessageFromErroCode()
	{
		switch(httpResponseCode)
		{
			case HttpsURLConnection.HTTP_FORBIDDEN: return ERR_FORBIDDEN;
			case HttpsURLConnection.HTTP_UNAUTHORIZED: return ERR_UNAUTHORIZD;
			case HttpsURLConnection.HTTP_UNAVAILABLE:return ERR_NOTREACHABLE;
			default: return "Unkown Error.("+ httpResponseCode+")";
		}
	}
	
	public V3Error()
	{
		
	}
	
	public V3Error(int httpcode , String respSring)
	{
		httpResponseCode = httpcode;
		
		if(respSring == null)
		{
			respSring = "Unknown Error";
		}
		
		/*
		 * special treatment to unauth and forbidden errors. the page can return error page in different formats 
		 */
		if( httpcode == HttpsURLConnection.HTTP_UNAUTHORIZED)
		{
			message.value = ERR_UNAUTHORIZD;
			return;
		}
		else if( httpcode == HttpsURLConnection.HTTP_FORBIDDEN)
		{
			message.value = ERR_FORBIDDEN;
			return;
		}
		else if( httpcode == HttpsURLConnection.HTTP_BAD_METHOD)
		{
			message.value = ERR_BADMETHOD;
			return;
		}
		
		try 
		{			
			SFLog.d2("-V3Error", "!!!V3ERROR construcor respStr = " + respSring);
			JSONObject errorObject = new JSONObject(respSring);			
			code =  errorObject.optString("code");			
			JSONObject messageObject = errorObject.getJSONObject("message");			
			message.value = messageObject.optString("value");
		} 
		catch (JSONException e) 
		{							
			message.value = respSring + "(" + httpcode + ")";			
			SFLog.d2("-V3Error", "Exception: " + Log.getStackTraceString(e));
		}		
	}
}