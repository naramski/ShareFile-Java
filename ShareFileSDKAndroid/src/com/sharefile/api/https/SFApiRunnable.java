package com.sharefile.api.https;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sharefile.api.SFApiQuery;
import com.sharefile.api.SFV3Error;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFV3ErrorException;
import com.sharefile.api.gson.SFGsonHelper;
import com.sharefile.api.gson.auto.SFDefaultGsonParser;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSymbolicLink;
import com.sharefile.java.log.SLog;

public class SFApiRunnable<T extends SFODataObject> implements Runnable 
{
	private static final String TAG = SFKeywords.TAG + "-SFApiRunnable";
			
	private SFApiQuery<T> mQuery; 
	private final SFApiResponseListener<T> mResponseListener;
	private final SFOAuth2Token mOauthToken;
	private final SFCookieManager mCookieManager;
		
	
	/**
	 *   This object will get filled with an errorCoode and the V3Error or valid SFOBject after the response 
	 *   The callListerners will be called appropriately based on the contents of this object.
	 */
	private class FinalResponse
	{
		private int mHttpErrorCode = 0;
		private SFV3Error mV3Error = null;
		private T mResponseObject = null;	
		
		public void setFields(int errorCode, SFV3Error v3Error, SFODataObject sfObject)
		{
			mHttpErrorCode = errorCode;
			mV3Error = v3Error;
			mResponseObject = (T)sfObject;
		}
	};
	
	FinalResponse mResponse = new FinalResponse();
		
	public SFApiRunnable(SFApiQuery<T> query, SFApiResponseListener<T> responseListener,SFOAuth2Token token,SFCookieManager cookieManager) throws SFInvalidStateException
	{			
		mQuery = query;
		mResponseListener = responseListener;
		mOauthToken = token;		
		mCookieManager = cookieManager;
	}
	
	@Override
	public void run() 
	{
		try 
		{
			executeQuery();
		} 
		catch (SFV3ErrorException e) 
		{			
			SLog.d(TAG, "Exception. This should not happen." , e);
		}
	}
	
	private void handleHttPost(URLConnection conn) throws IOException
	{
		if(mQuery.getHttpMethod().equalsIgnoreCase(SFHttpMethod.POST.toString()))
		{
			String body = mQuery.getBody(); 
			
			if(body!=null)
			{
				conn.setRequestProperty(SFKeywords.CONTENT_LENGTH, ""+body.getBytes().length);
				conn.setRequestProperty(SFKeywords.CONTENT_TYPE, SFKeywords.APPLICATION_JSON);
				
				SFHttpsCaller.postBody(conn, body);				
			}
		}
	}

	private String fillSpecialResponse(String downloadURl)
	{				
		try 
		{			
			JsonObject  jsonObject = new JsonObject();
			jsonObject.addProperty(SFKeywords.ODATA_METADATA, SFV3ElementType.DownloadSpecification.toString());
			jsonObject.addProperty(SFKeywords.DownloadUrl, downloadURl);
			return jsonObject.toString();
		} 
		catch (Exception e) 
		{			
			SLog.e(TAG,e);
		}
		
		return null;
	}
	
	
	public T executeQuery() throws SFV3ErrorException 
	{
		String server = mOauthToken.getApiServer();		
		String urlstr;
		try {
			urlstr = mQuery.buildQueryUrlString(server);
			
		} catch (UnsupportedEncodingException e) {
			SLog.e(TAG, e);
			return handleResponse(Log.getStackTraceString(e), SFSDK.INTERNAL_HTTP_ERROR);
		}
		
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// TODO: get rid of the special handling as it is not needed.
		if(mQuery.getNeedSpecialHandling()) {
			// hack for download
			return handleResponse(fillSpecialResponse(urlstr), HttpsURLConnection.HTTP_OK);
		}
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		String responseString = null;
		int httpErrorCode =  SFSDK.INTERNAL_HTTP_ERROR;
		URLConnection connection = null;
		
		try
		{
			URL url = new URL(urlstr);
			connection = SFHttpsCaller.getURLConnection(url);		
			SFHttpsCaller.setMethod(connection, mQuery.getHttpMethod());
			SFHttpsCaller.setAcceptLanguage(connection);
			
			SFHttpsCaller.addAuthenticationHeader(connection,mOauthToken,mQuery.getUserName(),mQuery.getPassword(),mCookieManager);
			
			handleHttPost(connection);
			
			SLog.d(TAG, mQuery.getHttpMethod() + " " + urlstr);
			
			connection.connect();
			
			httpErrorCode = SFHttpsCaller.safeGetResponseCode(connection);			
			
			//Use the bearer token currently. ignore the cookies untill we have a good cookie mgr. might impact sharepoint testing without cookies. 
			//v3Error = SFHttpsCaller.handleErrorAndCookies(connection, httpErrorCode, url);
			SFHttpsCaller.getAndStoreCookies(connection, url,mCookieManager);
		    
			if(httpErrorCode == HttpsURLConnection.HTTP_OK)
			{										
				responseString = SFHttpsCaller.readResponse(connection);
			}
			else if(httpErrorCode == HttpsURLConnection.HTTP_NO_CONTENT)
			{
				//no content. might be valid. let the listeners handle this.
			}
			else if(httpErrorCode == HttpsURLConnection.HTTP_MOVED_TEMP)
			{
				responseString = connection.getHeaderField(SFKeywords.Location);								
			}
			else
			{
				responseString = SFHttpsCaller.readErrorResponse(connection);
			}
				    
			SLog.v(TAG, "RAW RESPONSE = " + responseString);						
		}
		catch(Exception ex)
		{		
			SLog.e(TAG,ex);
			httpErrorCode = SFSDK.INTERNAL_HTTP_ERROR;
			responseString = "OrignalHttpCode = " + httpErrorCode + "\nExceptionStack = " +Log.getStackTraceString(ex);												
		}		
		finally
		{
			SFHttpsCaller.disconnect(connection);
		}
				
		return handleResponse(responseString, httpErrorCode);
	}

	private T handleResponse(String responseString, int httpErrorCode) throws SFV3ErrorException {
		try
		{
			parseResponse(httpErrorCode,responseString);		
		
			if(!readAheadSymbolicLinks())
			{
				callResponseListeners();
			}
			else
			{
				try 
				{
					return reExecuteNewQueryForSymbolicLinks();
				} 
				catch (URISyntaxException e) 
				{				
					SLog.e(TAG,e);
				}			
			}
		}
		catch(Exception e)
		{
			SLog.e(TAG,e);
		}
		
		return returnResultOrThrow();
	}		 
			
	/**
	 *   Parse the response to the best of our ability. At the end of this function the FinalResponse object 
	 *   has to be filled with an ErrorCode or HTTP_OK and the V3Error or SFOBject should be filled based on success or failure or 
	 *   response parsing.	 
	 */
	private void parseResponse(int httpCode,String responseString)
	{
		SFV3Error v3Error;
		
		switch(httpCode)
		{
			case HttpsURLConnection.HTTP_OK:
				callSuccessResponseParser(responseString);				
			break;	
			
			case HttpsURLConnection.HTTP_NO_CONTENT:
				mResponse.setFields(HttpsURLConnection.HTTP_NO_CONTENT, null, null);
			break;
			
			case HttpsURLConnection.HTTP_MOVED_TEMP:
				v3Error = new SFV3Error(httpCode,null,responseString);
				mResponse.setFields(HttpsURLConnection.HTTP_MOVED_TEMP, v3Error, null);
			break;
			
			case HttpsURLConnection.HTTP_UNAUTHORIZED:
				v3Error = new SFV3Error(httpCode,null,responseString);
				mResponse.setFields(HttpsURLConnection.HTTP_UNAUTHORIZED, v3Error, null);
			break;
			
			case SFSDK.INTERNAL_HTTP_ERROR:
				callInternalErrorResponseFiller(httpCode, responseString, null);
			break;
			
			default:
				callFailureResponseParser(httpCode, responseString);
			break;				
		}				
	}
	
	/**
	 *   If the returned object is a Symbolic Link we need to read ahead to get the actual contents.
	 *   return true if the object is SymbolicLink object and we are handling it internally else return false.
	 * @throws URISyntaxException 
	 * @throws SFV3ErrorException 
	 *   
	 */
	private boolean readAheadSymbolicLinks()
	{
		boolean ret = false;
		
		if(mResponse.mHttpErrorCode == HttpsURLConnection.HTTP_OK) 
		{			
			if(mResponse.mResponseObject instanceof SFSymbolicLink)
			{
				ret = true;
			}
		}
		
		return ret;
	}
	
	private T reExecuteNewQueryForSymbolicLinks() throws URISyntaxException, SFV3ErrorException
	{
		SFSymbolicLink link = (SFSymbolicLink) mResponse.mResponseObject;				
				
		//SFApiQuery<T> tempQuery = new SFApiQuery<T>(); //Build a new Query				
		//tempQuery.copyQuery(mQuery); //Copy the vital fields from the original query into the new query.		
		//tempQuery.setLink(link.getLink().toString()); //Override the symbolic link		
		//mQuery.copyQuery(tempQuery); //copy back
		
		//Dont create new query object. just replace the link
		mQuery.setLink(link.getLink().toString());
		
		return executeQuery();
	}
	
	private void callResponseListeners()
	{
		if(mResponseListener == null)
		{
			return;
		}
		
		try
		{
			switch(mResponse.mHttpErrorCode)
			{
				case HttpsURLConnection.HTTP_OK:
					mResponseListener.sfApiSuccess((T) mResponse.mResponseObject);				
				break;	
				
				case HttpsURLConnection.HTTP_NO_CONTENT:
					mResponseListener.sfApiSuccess(null);
				break;
												
				default:
					mResponseListener.sfApiError(mResponse.mV3Error, mQuery);
				break;				
			}
		}
		catch(Exception ex)
		{
			SLog.d(TAG, "!!Exception calling the responseListener ",ex);
		}
	}
	
	private T returnResultOrThrow() throws SFV3ErrorException
	{
		//Run this only when the responseListener is not installed.
		if(mResponseListener != null)
		{
			return null;
		}
		
		switch(mResponse.mHttpErrorCode)
		{
			case HttpsURLConnection.HTTP_OK:
			return mResponse.mResponseObject;							
			
			case HttpsURLConnection.HTTP_NO_CONTENT:
			return	null;										
		}
		
		throw new SFV3ErrorException(mResponse.mV3Error);
	}
		
	/**
	 *   This is a filler only. wont do any parsing.
	 */
	private void callInternalErrorResponseFiller(int httpCode,String errorDetails,String extraInfo)
	{
		SFV3Error v3Error = new SFV3Error(httpCode,errorDetails,extraInfo);
		mResponse.setFields(SFSDK.INTERNAL_HTTP_ERROR, v3Error, null);
	}
				
	/**
	 *  If an error happens during parsing the success response, 
	 *  we return the exception description + the original server response in V3Error Object
	 */
	private void callSuccessResponseParser(String responseString)
	{					
		try 
		{			
			JsonParser jsonParser = new JsonParser();
			JsonElement jsonElement =jsonParser.parse(responseString);
			SFODataObject object = SFGsonHelper.customParse(jsonElement);			
			mResponse.setFields(HttpsURLConnection.HTTP_OK, null, object);			
		} 
		catch (Exception e) 
		{					
			/* 
			 * Note how we fill the httpErrorcode in V3Object to 200. Thats coz the server originally returned 200, 
			 * just the response object was malformed or caused some other exception while parsing.			 
			 */						
			callInternalErrorResponseFiller(HttpsURLConnection.HTTP_OK,Log.getStackTraceString(e),responseString);
		}					
	}
	
	private void callFailureResponseParser(int httpCode, String responseString)
	{
													
		try 
		{
			JsonParser jsonParser = new JsonParser();
			JsonElement jsonElement =jsonParser.parse(responseString);				
			SFV3Error v3Error = SFDefaultGsonParser.parse(jsonElement);
			v3Error.httpResponseCode = httpCode;				
			mResponse.setFields(httpCode, v3Error, null);
		} 
		catch (Exception e)  
		{					
			/* 
			 * Note how we fill the httpErrorcode to httpCode. Thats coz the server originally returned it, 
			 * just the error object was malformed or caused some other exception while parsing.			 
			 */						
			callInternalErrorResponseFiller(httpCode,Log.getStackTraceString(e),responseString);
		}
	}
			
	
	public Thread startNewThread()
	{
		Thread sfApithread = new Thread(this);		
		sfApithread.start();
		return sfApithread;
	}				
}