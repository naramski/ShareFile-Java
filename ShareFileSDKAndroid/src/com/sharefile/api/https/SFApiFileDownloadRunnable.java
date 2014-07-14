package com.sharefile.api.https;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sharefile.api.SFApiClient;
import com.sharefile.api.SFV3Error;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.gson.auto.SFDefaultGsonParser;
import com.sharefile.api.interfaces.SFApiDownloadProgressListener;
import com.sharefile.api.models.SFDownloadSpecification;
import com.sharefile.java.log.SLog;

public class SFApiFileDownloadRunnable implements Runnable  
{	
	private static final String TAG = SFKeywords.TAG + "-download";
	
	private final SFDownloadSpecification mDownloadSpecification;
	private final long mResumeFromByteIndex;
	private final OutputStream mOutputStream;
	private final SFApiClient mApiClient;
	private SFApiDownloadProgressListener mProgressListener;
	private FinalResponse mResponse = new FinalResponse();	
	private final SFCookieManager mCookieManager;
	private final AtomicBoolean cancelRequested = new AtomicBoolean(false);
	private Thread mApithread = null; 
	
	//credntials for connectors
		private final String mUsername;
		private final String mPassword;
	
	public SFApiFileDownloadRunnable(SFDownloadSpecification downloadSpecification,
									 int resumeFromByteIndex, 
									 OutputStream outpuStream, 
									 SFApiClient client,
									 SFApiDownloadProgressListener progressListener,SFCookieManager cookieManager,String connUserName,String connPassword) 
	{		
		mDownloadSpecification = downloadSpecification;
		mResumeFromByteIndex = resumeFromByteIndex;
		mOutputStream = outpuStream;
		mApiClient = client;
		mProgressListener = progressListener;
		mCookieManager = cookieManager;
		mUsername = connUserName;
		mPassword = connPassword;
	}

	@Override
	public void run()  {
		try {
			download();
			
		} finally {
			mApithread = null;
		}
	}
	
	public void cancel() {
		cancelRequested.set(true);
	}
	
	public void download()
	{
		int httpErrorCode =  SFSDK.INTERNAL_HTTP_ERROR;
		String responseString = null;
		long bytesRead = mResumeFromByteIndex;
		URLConnection connection = null;
		InputStream fis = null;
		
		try
		{										
			SLog.d(TAG, "GET " + mDownloadSpecification.getDownloadUrl());
			
			URL url = mDownloadSpecification.getDownloadUrl().toURL();
			connection = SFHttpsCaller.getURLConnection(url);		
			SFHttpsCaller.setMethod(connection, SFHttpMethod.GET.toString());
			SFHttpsCaller.setAcceptLanguage(connection);
			SFHttpsCaller.addAuthenticationHeader(connection,mApiClient.getAuthToken(),mUsername,mPassword,mCookieManager);
			
			if(mResumeFromByteIndex!=0)
			{
				connection.setRequestProperty(SFKeywords.Range, "bytes="+mResumeFromByteIndex+"-");
			}
																				
			connection.connect();
			
			httpErrorCode = SFHttpsCaller.safeGetResponseCode(connection);			
						
			SFHttpsCaller.getAndStoreCookies(connection, url,mCookieManager);
		    
			if(httpErrorCode == HttpsURLConnection.HTTP_OK)
			{														
				fis = connection.getInputStream();
				
				byte[] buffer = new byte[1024 * 1024];
				
				int length = 0;
				
				while ((length = fis.read(buffer)) > 0) 
				{
					if ( cancelRequested.get() ) break;
					
					mOutputStream.write(buffer, 0, length);
					bytesRead+= length;
					updateProgress(bytesRead);
				}				
			}
			else if(httpErrorCode == HttpsURLConnection.HTTP_NO_CONTENT)
			{
				
			}
			else
			{
				responseString = SFHttpsCaller.readErrorResponse(connection);
			}
				    									
		}
		catch(Exception ex)
		{		
			httpErrorCode = SFSDK.INTERNAL_HTTP_ERROR;
			responseString = "OrignalHttpCode = " + httpErrorCode + "\nExceptionStack = " +Log.getStackTraceString(ex);												
		}
		finally
		{
			closeStream(fis);
			closeStream(mOutputStream);
			SFHttpsCaller.disconnect(connection);
		}
				
		parseResponse(httpErrorCode,responseString,bytesRead);
		
		callResponseListeners();

	}
	
	private void closeStream(Closeable fis)
	{
		if(fis!=null)
		{
			try 
			{
				fis.close();
			}
			catch (IOException e) 
			{				
				SLog.e(TAG,e);
			}
		}
	}
	
	/**
	 *   This object will get filled with an errorCoode and the V3Error or valid SFOBject after the response 
	 *   The callListerners will be called appropriately based on the contents of this object.
	 */
	private class FinalResponse
	{
		private int mHttpErrorCode = 0;
		private SFV3Error mV3Error = null;			
		private long mBytesDownloaded = 0;
		
		public void setFields(int errorCode, SFV3Error v3Error, long downloaded)
		{
			mHttpErrorCode = errorCode;
			mV3Error = v3Error;			
			mBytesDownloaded = downloaded;
		}
	};
	
	/**
	 *   Parse the response to the best of our ability. At the end of this function the FinalResponse object 
	 *   has to be filled with an ErrorCode or HTTP_OK and the V3Error or SFOBject should be filled based on success or failure or 
	 *   response parsing.	 
	 */
	private void parseResponse(int httpCode,String responseString,long downloadedBytes)
	{
		if ( cancelRequested.get() ) {
			// download was cancelled: no reason to process response 
			return;
		}
		
		switch(httpCode)
		{
			case HttpsURLConnection.HTTP_OK:
				mResponse.setFields(HttpsURLConnection.HTTP_OK, null,downloadedBytes);
			break;	
			
			case HttpsURLConnection.HTTP_NO_CONTENT:
				mResponse.setFields(HttpsURLConnection.HTTP_NO_CONTENT, null,downloadedBytes);
			break;
			
			case HttpsURLConnection.HTTP_UNAUTHORIZED:
				SFV3Error v3Error = new SFV3Error(httpCode,null,responseString);
				mResponse.setFields(HttpsURLConnection.HTTP_UNAUTHORIZED, v3Error,downloadedBytes);
			break;
			
			case SFSDK.INTERNAL_HTTP_ERROR:
				callInternalErrorResponseFiller(httpCode, responseString,null,downloadedBytes);
			break;
			
			default:
				callFailureResponseParser(httpCode, responseString,downloadedBytes);
			break;				
		}				
	}
	
	private void callFailureResponseParser(int httpCode, String responseString,long downloadedBytes)
	{													
		try 
		{
			JsonParser jsonParser = new JsonParser();
			JsonElement jsonElement =jsonParser.parse(responseString);				
			SFV3Error v3Error = SFDefaultGsonParser.parse(jsonElement);
			v3Error.httpResponseCode = httpCode;				
			mResponse.setFields(httpCode, v3Error,downloadedBytes);
		} 
		catch (Exception e)  
		{					
			/* 
			 * Note how we fill the httpErrorcode to httpCode. Thats coz the server originally returned it, 
			 * just the error object was malformed or caused some other exception while parsing.			 
			 */						
			callInternalErrorResponseFiller(httpCode,Log.getStackTraceString(e),responseString,downloadedBytes);
		}
	}
	
	private void updateProgress(long downloadedBytes)
	{
		if(mProgressListener == null)
		{
			return;
		}
		
		try
		{
			mProgressListener.bytesDownloaded(downloadedBytes, mDownloadSpecification, mApiClient);
		}
		catch(Exception e)
		{
			SLog.d(TAG, "exception in updateProgress" , e);
		}		
	}
	
	private void callResponseListeners()
	{
		if(mProgressListener == null)
		{
			return;
		}
		
		try
		{
			if ( cancelRequested.get() ) {
				SLog.d(TAG, "Download cancelled");
				mProgressListener.downloadCanceled(mResponse.mBytesDownloaded, mDownloadSpecification, mApiClient);
				return;
			}
			
			switch(mResponse.mHttpErrorCode)
			{
				case HttpsURLConnection.HTTP_OK:
					mProgressListener.downloadSuccess(mResponse.mBytesDownloaded, mDownloadSpecification, mApiClient);				
				break;	
																
				default:
					mProgressListener.downloadFailure(mResponse.mV3Error,mResponse.mBytesDownloaded, mDownloadSpecification, mApiClient);
				break;				
			}
			
		} catch(Exception ex) {
			SLog.d(TAG, "!!Exception calling the responseListener",ex);
			
		} finally {
			// 
			mProgressListener = null;
		}
	}
	
	/**
	 *   This is a filler only. wont do any parsing.
	 */
	private void callInternalErrorResponseFiller(int httpCode,String errorDetails,String extraInfo,long bytesDownloaded)
	{
		SFV3Error v3Error = new SFV3Error(httpCode,errorDetails,extraInfo);
		mResponse.setFields(SFSDK.INTERNAL_HTTP_ERROR, v3Error,bytesDownloaded);
	}
	
	public Thread startNewThread() {
		if ( mApithread!=null ) {
			SLog.w(TAG, "There is already a thread processing this download");
			// ...
		}
		
		mApithread = new Thread(this);		
		mApithread.start();
		return mApithread;
	}	
}