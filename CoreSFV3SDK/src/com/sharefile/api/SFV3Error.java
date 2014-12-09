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
	private static final String ERR_FORBIDDEN =   "Forbidden (403)";
	private static final String ERR_UNAUTHORIZD = "Unauthorized (401)";
	private static final String ERR_NOTREACHABLE = "Server Not reachable (503)";
	private static final String ERR_BADMETHOD = "Method not allowed (405)";
					
	protected Exception mInternalException = null;
    protected int httpResponseCode = SFSDK.INTERNAL_HTTP_ERROR;
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
		catch (Exception e) 
		{										
			mInternalException = exception;			
		}
	}

    public SFV3Error(int errorCode, String message) {
        httpResponseCode = 200; // ???
        code = String.valueOf(errorCode);
        value = message;
    }

    protected SFV3Error(int serverHttpCode ,Exception exception)
    {
        httpResponseCode = serverHttpCode;
        mInternalException = exception ;
    }
				
	public boolean isAuthError()
	{
		return httpResponseCode == HttpsURLConnection.HTTP_UNAUTHORIZED;
	}

    public boolean isCancelled()
    {
        return httpResponseCode == SFSDK.HTTP_ERROR_CANCELED;
    }

    public boolean isConnectionError()
    {
        return httpResponseCode == SFSDK.INTERNAL_HTTP_ERROR_NETWORK_CONNECTION_PROBLEM;
    }
	/**
	 *  Allows the clients to show a localized message if it is sent from the server or optional string if its an internal error
	 */
	public String errorDisplayString(String optionalLocalized)
	{
        if(httpResponseCode == SFSDK.INTERNAL_HTTP_ERROR_NETWORK_CONNECTION_PROBLEM)
        {
            return "Cannot connect to network";
        }
		else if(httpResponseCode != SFSDK.INTERNAL_HTTP_ERROR && value!=null)
		{
			return value;
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
	
    public int getHttpResponseCode()
    {
        return httpResponseCode;
    }

	public Exception getException()
	{
		return mInternalException;
	}
}