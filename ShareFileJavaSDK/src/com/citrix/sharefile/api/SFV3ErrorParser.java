package com.citrix.sharefile.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.citrix.sharefile.api.constants.SFKeywords;
import com.citrix.sharefile.api.constants.SFSdkGlobals;
import com.citrix.sharefile.api.gson.SFGsonHelper;

import javax.net.ssl.HttpsURLConnection;

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

public class SFV3ErrorParser
{
	private static final String ERR_FORBIDDEN =   "Forbidden (403)";
	private static final String ERR_UNAUTHORIZD = "Unauthorized (401)";
	private static final String ERR_NOTREACHABLE = "Server Not reachable (503)";
	private static final String ERR_BADMETHOD = "Method not allowed (405)";
					
	protected Exception mInternalException = null;
    protected int httpResponseCode = SFSdkGlobals.INTERNAL_HTTP_ERROR;
    protected String code = "";
    protected String lang = null;
    protected String value = null;
    
	protected String getErrorMessageFromErroCode(int httpResponseCode)
	{
		switch(httpResponseCode)
		{
			case HttpsURLConnection.HTTP_FORBIDDEN: return ERR_FORBIDDEN;
			case HttpsURLConnection.HTTP_UNAUTHORIZED: return ERR_UNAUTHORIZD;
			case HttpsURLConnection.HTTP_UNAVAILABLE:return ERR_NOTREACHABLE;
			case HttpsURLConnection.HTTP_BAD_METHOD:return ERR_BADMETHOD;
			default: return SFKeywords.UNKNOWN_ERROR + " : "+ httpResponseCode;
		}
	}

	/**
	 *   V3Error are JSON objects. It might happen that the server returns a Non-Json object responsestring/or something we got from an http exception causing 
	 *   a JSONException in this constructor. In such case, the constructor simply returns the following:
	 *   <p>httpError = original code
	 *   <p>message.value = Exception stack
	 *   <p>mExtraInfo = original response string which we tried to parse
	 */
	public SFV3ErrorParser(int serverHttpCode, String serverRespSring, Exception exception)
	{
        httpResponseCode = serverHttpCode;
		mInternalException = exception ;
		
		if(serverRespSring == null)
		{
			value = getErrorMessageFromErroCode(serverHttpCode);
			return;
		}
										
		try 
		{											
			JsonParser jsonParser = new JsonParser();
			JsonElement jsonElement = jsonParser.parse(serverRespSring);
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			
			code = SFGsonHelper.getString(jsonObject, SFKeywords.CODE, "");
			
			JsonObject messageObject = jsonObject.getAsJsonObject(SFKeywords.MESSAGE);
			value = SFGsonHelper.getString(messageObject, SFKeywords.VALUE, "");			
		} 
		catch (Throwable e)
		{										
			mInternalException = exception;			
		}
	}

	public String errorDisplayString()
	{
        if(httpResponseCode != SFSdkGlobals.INTERNAL_HTTP_ERROR && value!=null)
        {
            return value;
        }

        if(httpResponseCode == SFSdkGlobals.INTERNAL_HTTP_ERROR_NETWORK_CONNECTION_PROBLEM)
        {
            return "Cannot connect to network";
        }

        if(mInternalException!=null && mInternalException.getLocalizedMessage()!=null)
		{
			return mInternalException.getLocalizedMessage();
		}
		
		return SFKeywords.UNKNOWN_ERROR;
	}

    public Exception getException()
	{
		return mInternalException;
	}
}