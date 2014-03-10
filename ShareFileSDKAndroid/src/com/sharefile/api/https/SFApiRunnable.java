package com.sharefile.api.https;

import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import android.util.Log;

import com.sharefile.api.SFApiQuery;
import com.sharefile.api.V3Error;
import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSession;

public class SFApiRunnable<T extends SFODataObject> implements Runnable 
{
	private static final String TAG = "-SFApiThread";
	private final SFApiQuery<T> mQuery; 
	private final SFApiResponseListener<T> mResponseListener;
	private final SFOAuth2Token mOauthToken;
	
	public SFApiRunnable(SFApiQuery<T> query, SFApiResponseListener<T> responseListener,SFOAuth2Token token) throws SFInvalidStateException
	{		
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
		int httpErrorCode = 0;
		V3Error v3Error = null;
		
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
			
			httpErrorCode = SFHttpsCaller.safeGetResponseCode(connection);
			
			//v3Error = SFHttpsCaller.handleErrorAndCookies(connection, httpErrorCode, url);
		    
			if(httpErrorCode == HttpsURLConnection.HTTP_OK)
			{											
				String responseString = SFHttpsCaller.readResponse(connection);
				
				SFSession session = new SFSession();
				
				session.parseFromJson(responseString);
			}
			else
			{
				String errorResonseString = SFHttpsCaller.readErrorResponse(connection);
			}
				    
			SFHttpsCaller.disconnect(connection);
		}
		catch(Exception ex)
		{
			SFLog.d2(TAG, "%s",Log.getStackTraceString(ex));
		}		
	}		 
		
	public Thread startNewThread()
	{
		Thread sfApithread = new Thread(this);		
		sfApithread.start();
		return sfApithread;
	}		
}
