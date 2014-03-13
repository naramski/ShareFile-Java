package com.sharefile.api.https;

import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sharefile.api.SFApiQuery;
import com.sharefile.api.V3Error;
import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFJsonException;
import com.sharefile.api.gson.auto.SFDefaultGsonParser;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFODataObject;

public class SFApiRunnable<T extends SFODataObject> implements Runnable 
{
	private static final String TAG = "-SFApiThread";
	
	private final Class<T> mInnerType ;	
	private final SFApiQuery<T> mQuery; 
	private final SFApiResponseListener<T> mResponseListener;
	private final SFOAuth2Token mOauthToken;
	
	private int mHttpErrorCode = 0;
	private V3Error mV3Error = null;
	private String mResponseString = null;
		
	public SFApiRunnable(Class<T> innerType, SFApiQuery<T> query, SFApiResponseListener<T> responseListener,SFOAuth2Token token) throws SFInvalidStateException
	{	
		mInnerType = innerType;
		mQuery = query;
		mResponseListener = responseListener;
		mOauthToken = token;		
	}
	
	@Override
	public void run() 
	{
		executeQuery();
	}
	
	public void executeQuery() 
	{								
		String server = mOauthToken.getApiServer();		
		String urlstr = mQuery.buildQueryUrlString(server);
				
		try
		{
			URL url = new URL(urlstr);
			URLConnection connection = SFHttpsCaller.getURLConnection(url);		
			SFHttpsCaller.setMethod(connection, mQuery.getHttpMethod());		
			SFHttpsCaller.addBearerAuthorizationHeader(connection, mOauthToken);
			
			SFLog.d2(TAG, mQuery.getHttpMethod() + " " + urlstr);
			
			connection.connect();
			
			mHttpErrorCode = SFHttpsCaller.safeGetResponseCode(connection);
			
			//Use the bearer token currently. ignore the cookies untill we have a good cookie mgr. might impact sharepoint testing without cookies. 
			//v3Error = SFHttpsCaller.handleErrorAndCookies(connection, httpErrorCode, url);
		    
			if(mHttpErrorCode == HttpsURLConnection.HTTP_OK)
			{											
				mResponseString = SFHttpsCaller.readResponse(connection);								
			}
			else if(mHttpErrorCode == HttpsURLConnection.HTTP_NO_CONTENT)
			{
				//no content. might be valid. let the listeners handle this.
			}
			else
			{
				mResponseString = SFHttpsCaller.readErrorResponse(connection);
			}
				    
			SFHttpsCaller.disconnect(connection);
		}
		catch(Exception ex)
		{
			mHttpErrorCode = SFSDK.INTERNAL_HTTP_ERROR;
			//mResponseString =?? TODO: Fill the response string with a customized error describing the exception in this case.
			SFLog.d2(TAG, "%s",Log.getStackTraceString(ex));
		}		
		
		processResponse();				
	}		 
		
	/**
	 * 	the mHttpErrorCode and mResponseString are expected to be filled appropriately by this stage.
	 */
	private void processResponse()
	{
		switch(mHttpErrorCode)
		{
			case HttpsURLConnection.HTTP_OK:
				
				callSuccessResponseHandler();
			break;	
			
			case HttpsURLConnection.HTTP_NO_CONTENT:
				callEmptyResponseHandler();
			break;
			
			default:
				callFailureResponseHandler();
			break;				
		}
	}
	
	private void callSuccessResponseHandler()
	{
		if(mResponseListener!=null)
		{						
			//T object = createInstanceForSuccessResponse();			
			try 
			{
				JsonParser jsonParser = new JsonParser();
				JsonElement jsonElement =jsonParser.parse(mResponseString);
				SFODataObject object = SFDefaultGsonParser.parse(mInnerType, jsonElement);
				mResponseListener.sfapiSuccess((T) object);
			} 
			catch (Exception e) 
			{					
				e.printStackTrace();
			}			
		}
	}
	
	private void callFailureResponseHandler()
	{
		
	}
	
	private void callEmptyResponseHandler()
	{
		
	}
	
	public Thread startNewThread()
	{
		Thread sfApithread = new Thread(this);		
		sfApithread.start();
		return sfApithread;
	}		
	
	public T createInstanceForSuccessResponse()
	{		
		T object =	null;
		
		try 
		{
			object = mInnerType.newInstance();	
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
		} 		
						
		return object;
	}
}