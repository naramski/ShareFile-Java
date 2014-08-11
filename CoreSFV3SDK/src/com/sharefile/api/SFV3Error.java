package com.sharefile.api;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.gson.SFGsonHelper;

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
public class SFV3Error 
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
	
	public SFV3Error()
	{
		
	}
		
	/**
	 *   V3Error are JSON objects. It might happen that the server returns a Non-Json object responsestring/or something we got from an http exception causing 
	 *   a JSONException in this constructor. In such case, the constructor simply returns the following:
	 *   <p>httpError = original code
	 *   <p>message.value = Exception stack
	 *   <p>mExtraInfo = original response string which we tried to parse
	 */
	public SFV3Error(int httpcode , String respSring)
	{				
		httpResponseCode = httpcode;
		mExtraInfo = null;
						
		if(respSring == null)
		{
			respSring = getErrorMessageFromErroCode();
		}
				
		try 
		{											
			JsonParser jsonParser = new JsonParser();
			JsonElement jsonElement = jsonParser.parse(respSring);
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			
			code = SFGsonHelper.getString(jsonObject, SFKeywords.CODE, "");
			
			JsonObject messageObject = jsonObject.getAsJsonObject(SFKeywords.MESSAGE);
			message.value = SFGsonHelper.getString(messageObject, SFKeywords.VALUE, "");			
		} 
		catch (Exception e) 
		{							
			message.value = "Exception during constructing V3Error. See extraInfo for more details of server response";
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
	public SFV3Error(int httpcode , String respSring, String extraInfo)
	{
		httpResponseCode = httpcode;
		mExtraInfo = extraInfo;	
		
		if(respSring == null)
		{
			respSring = getErrorMessageFromErroCode();
		}
		
		message.value = respSring;				
	}
	
	public boolean isAuthError()
	{
		if(httpResponseCode == HttpsURLConnection.HTTP_UNAUTHORIZED)
		{
			return true;
		}
		
		return false;
	}
	
	
	/**
	 *  Allows the clients to show a localized message if its sent from the server or optional string if its an internal error
	 */
	public String errorDisplayString(String optionalLocalized)
	{
		if(httpResponseCode != SFSDK.INTERNAL_HTTP_ERROR && message!=null)
		{
			return message.value;
		}
		
		return optionalLocalized;
	}
}