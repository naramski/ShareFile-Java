package com.sharefile.api;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import com.sharefile.api.constants.SFKeywords;

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
	
	public String mExtraInfo = null;	
	
	public static class ErrorMessage
	{
		public String lang = null;
		public String value = null;
	}
	
	public ErrorMessage message = new ErrorMessage();
			
	
	private String getErrorMessageFromErroCode()
	{
		switch(httpResponseCode)
		{
			case HttpsURLConnection.HTTP_FORBIDDEN: return ERR_FORBIDDEN;
			case HttpsURLConnection.HTTP_UNAUTHORIZED: return ERR_UNAUTHORIZD;
			case HttpsURLConnection.HTTP_UNAVAILABLE:return ERR_NOTREACHABLE;
			case HttpsURLConnection.HTTP_BAD_METHOD:return ERR_BADMETHOD;
			default: return "Unkown Error.("+ httpResponseCode+")";
		}
	}
	
	public V3Error()
	{
		
	}
		
	/**
	 *   V3Error are JSON objects. It might happen that the server returns a Non-Json object responsestring/or something we got from an http exception causing 
	 *   a JSONException in this constructor. In such case, the constructor simply returns the following:
	 *   <p>httpError = original code
	 *   <p>message.value = Exception stack
	 *   <p>mExtraInfo = original response string which we tried to parse
	 */
	public V3Error(int httpcode , String respSring)
	{				
		httpResponseCode = httpcode;
		mExtraInfo = null;
						
		if(respSring == null)
		{
			respSring = getErrorMessageFromErroCode();
		}
				
		try 
		{						
			JSONObject errorObject = new JSONObject(respSring);			
			code =  errorObject.optString(SFKeywords.CODE);			
			JSONObject messageObject = errorObject.getJSONObject(SFKeywords.MESSAGE);			
			message.value = messageObject.optString(SFKeywords.VALUE);
		} 
		catch (JSONException e) 
		{							
			message.value = "JSON Exception during constructing V3Error. See extraInfo for more details of server response";
			mExtraInfo = respSring;			
		}
	}
				
	/**
	 *   Call this constructor only for non-server error parsing. V3Error are JSON objects. It might happen that the server returns a Non-Json object responsestring/or something we got from an http exception causing 
	 *   a JSONException in this constructor. In such case, the constructor simply returns the following:
	 *   <p>httpError = original code
	 *   <p>message.value = Exception stack
	 *   <p>mExtraInfo = original response string which we tried to parse
	 */
	public V3Error(int httpcode , String respSring, String extraInfo)
	{
		httpResponseCode = httpcode;
		mExtraInfo = extraInfo;	
		
		if(respSring == null)
		{
			respSring = getErrorMessageFromErroCode();
		}
		
		message.value = respSring;				
	}
}