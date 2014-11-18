package com.sharefile.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.exceptions.SFOutOfMemoryException;
import com.sharefile.api.gson.SFGsonHelper;

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
public class SFV3Error 
{
	public static class ServerResponse
	{
		public int httpResponseCode = SFSDK.INTERNAL_HTTP_ERROR;
		public String code = "";
		public String lang = null;
		public String value = null;
	}
	
	private static final String ERR_FORBIDDEN =   "Forbidden (403)";
	private static final String ERR_UNAUTHORIZD = "Unauthorized (401)";
	private static final String ERR_NOTREACHABLE = "Server Not reachable (503)";
	private static final String ERR_BADMETHOD = "Method not allowed (405)";
					
	@SFSDKDefaultAccessScope Exception mInternalException = null;
	@SFSDKDefaultAccessScope ServerResponse mServerResponse = new ServerResponse();
				
	@SFSDKDefaultAccessScope String getErrorMessageFromErroCode(int httpResponseCode)
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
			
	/**
	 *   V3Error are JSON objects. It might happen that the server returns a Non-Json object responsestring/or something we got from an http exception causing 
	 *   a JSONException in this constructor. In such case, the constructor simply returns the following:
	 *   <p>httpError = original code
	 *   <p>message.value = Exception stack
	 *   <p>mExtraInfo = original response string which we tried to parse
	 */
	public SFV3Error(int serverHttpCode , String serverRespSring, Exception exception)
	{				
		mServerResponse.httpResponseCode = serverHttpCode;		
		mInternalException = exception ;
		
		if(serverRespSring == null)
		{
			mServerResponse.value = getErrorMessageFromErroCode(serverHttpCode);
			return;
		}
										
		try 
		{											
			JsonParser jsonParser = new JsonParser();
			JsonElement jsonElement = jsonParser.parse(serverRespSring);
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			
			mServerResponse.code = SFGsonHelper.getString(jsonObject, SFKeywords.CODE, "");
			
			JsonObject messageObject = jsonObject.getAsJsonObject(SFKeywords.MESSAGE);
			mServerResponse.value = SFGsonHelper.getString(messageObject, SFKeywords.VALUE, "");			
		} 
		catch (Exception e) 
		{										
			mInternalException = exception;			
		}
	}

    public SFV3Error(int errorCode, String message) {
        mServerResponse.httpResponseCode = 200; // ???
        mServerResponse.code = String.valueOf(errorCode);
        mServerResponse.value = message;
    }

    protected SFV3Error(int serverHttpCode ,Exception exception)
    {
        mServerResponse.httpResponseCode = serverHttpCode;
        mInternalException = exception ;
    }
				
	public boolean isAuthError()
	{
		if(mServerResponse.httpResponseCode == HttpsURLConnection.HTTP_UNAUTHORIZED)
		{
			return true;
		}
		
		return false;
	}

    public boolean isCancelled()
    {
        return (mServerResponse.httpResponseCode == SFSDK.HTTP_ERROR_CANCELED);
    }

    public boolean isConnectionError()
    {
        if(mServerResponse.httpResponseCode == SFSDK.INTERNAL_HTTP_ERROR_NETWORK_CONNECTION_PROBLEM)
        {
            return true;
        }

        return false;
    }
	/**
	 *  Allows the clients to show a localized message if it is sent from the server or optional string if its an internal error
	 */
	public String errorDisplayString(String optionalLocalized)
	{
        if(mServerResponse.httpResponseCode == SFSDK.INTERNAL_HTTP_ERROR_NETWORK_CONNECTION_PROBLEM)
        {
            return "Cannot connect to network";
        }
		else if(mServerResponse.httpResponseCode != SFSDK.INTERNAL_HTTP_ERROR && mServerResponse.value!=null)
		{
			return mServerResponse.value;
		}
		else if(mInternalException!=null && mInternalException.getLocalizedMessage()!=null)
		{
			if(mInternalException instanceof SFOutOfMemoryException)
			{
				return "Out of Memory";
			}
			
			return mInternalException.getLocalizedMessage();
		}
		
		return optionalLocalized;
	}
	
	public ServerResponse getServerResponse()
	{
		return mServerResponse;
	}
	
	public Exception getException()
	{
		return mInternalException;
	}
}