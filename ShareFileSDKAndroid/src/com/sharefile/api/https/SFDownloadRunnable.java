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
import com.sharefile.java.log.SLog;

public class SFDownloadRunnable extends TransferRunnable {
	private static final String TAG = SFKeywords.TAG + "-FileDownloadRunnable";
	
	private String mUrl;
	private final long mResumeFromByteIndex;
	private final OutputStream mOutputStream;
	private final SFApiClient mApiClient;
	private IProgress mProgressListener;
	private final Result mResponse = new Result();	
	private final SFCookieManager mCookieManager;

	//credentials for connectors
	private final String mUsername;
	private final String mPassword;

	// current transfer
	private int httpErrorCode =  SFSDK.INTERNAL_HTTP_ERROR;
	private String responseString = null;
	private long bytesRead = 0;

	private AtomicBoolean cancelRequested = new AtomicBoolean(false);
	

	public SFDownloadRunnable(String url,
									 int resumeFromByteIndex, 
									 OutputStream outpuStream, 
									 SFApiClient client,
									 IProgress progressListener,SFCookieManager cookieManager,String connUserName,String connPassword) 
	{		
		mUrl = url;
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
		runInThisThread();
	}
	
	/**
	 * execute download in this thread overriding the cancel signal
	 * @param cancel
	 * @return
	 */
	public Result runInThisThread(AtomicBoolean cancel) {
		cancelRequested = cancel;
		return runInThisThread();
	}
	
	/**
	 * execute download in this thread
	 * @return
	 */
	public Result runInThisThread() {
		try {
			download();
		
		} catch(Exception ex) {		
			httpErrorCode = SFSDK.INTERNAL_HTTP_ERROR;
			responseString = "OrignalHttpCode = " + httpErrorCode + "\nExceptionStack = " +Log.getStackTraceString(ex);												
		}
		
		if ( isCanceled() ) {
			// create "cancel error" regardless of result			
			return createCancelResult(bytesRead);
		}
		
		parseResponse(httpErrorCode, responseString, bytesRead);

		return mResponse;
	}
	
	/**
	 * request the current download to cancel
	 */
	public void cancel() {
		cancelRequested.set(true);
	}
	
	private void download() throws IOException {
		bytesRead = mResumeFromByteIndex;
		
		URLConnection connection = null;
		InputStream fis = null;
		
		try
		{										
			SLog.d(TAG, "GET " + mUrl);
			
			URL url = new URL(mUrl);
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
					if ( isCanceled() ) break;
					
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
				    									
		} finally {
			closeStream(fis);
			closeStream(mOutputStream);
			SFHttpsCaller.disconnect(connection);
		}
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
	 *   Parse the response to the best of our ability. At the end of this function the FinalResponse object 
	 *   has to be filled with an ErrorCode or HTTP_OK and the V3Error or SFOBject should be filled based on success or failure or 
	 *   response parsing.	 
	 */
	private void parseResponse(int httpCode,String responseString,long downloadedBytes) {
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
		if(mProgressListener == null) {
			return;
		}
		
		try {
			mProgressListener.bytesTransfered(downloadedBytes);
			
		} catch(Exception e) {
			SLog.d(TAG, "exception in updateProgress" , e);
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

	public Result getResponse() {
		return mResponse;
	}

	public String getUrl() {
		return mUrl;
	}
	
	public boolean isCanceled() {
		return cancelRequested.get();
	}
}