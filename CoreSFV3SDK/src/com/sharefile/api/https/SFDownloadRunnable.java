package com.sharefile.api.https;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sharefile.api.SFApiClient;

import com.sharefile.api.SFV3ErrorParser;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.exceptions.SFNotAuthorizedException;
import com.sharefile.api.exceptions.SFOtherException;
import com.sharefile.api.exceptions.SFSDKException;
import com.sharefile.api.exceptions.SFServerException;
import com.sharefile.api.gson.auto.SFDefaultGsonParser;
import com.sharefile.api.log.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;

public class SFDownloadRunnable extends TransferRunnable {
	private static final String TAG = SFKeywords.TAG + "-FileDownloadRunnable";
	
	private String mUrl;
	private final long mResumeFromByteIndex;
	private final OutputStream mOutputStream;
	private final Result mResponse = new Result();

	// current transfer
	private int httpErrorCode =  SFSDK.INTERNAL_HTTP_ERROR;
	private String responseString = null;
	private long bytesRead = 0;

	public SFDownloadRunnable(String url,
									 int resumeFromByteIndex, 
									 OutputStream outpuStream, 
									 SFApiClient client,
									 IProgress progressListener,SFCookieManager cookieManager,String connUserName,String connPassword) 
	{
        super(client,progressListener,cookieManager,connUserName,connPassword);
		mUrl = url;
		mResumeFromByteIndex = resumeFromByteIndex;
		mOutputStream = outpuStream;
	}

	/**
	 * execute download in this thread
	 * @return
	 */
	protected Result runInThisThread() {
		try
        {
			download();
            parseResponse(httpErrorCode, responseString, bytesRead);
		}
        catch(Exception ex)
        {
		    callInternalErrorResponseFiller(ex,bytesRead);
		}
		
		if ( isCanceled() )
        {
			// create "cancel error" regardless of result
			return createCancelResult(bytesRead);
		}

		return mResponse;
	}

	private void download() throws IOException {
		bytesRead = mResumeFromByteIndex;
		
		URLConnection connection = null;
		InputStream fis = null;
		
		try
		{										
			Logger.d(TAG, "GET " + mUrl);
			
			URL url = new URL(mUrl);
			connection = SFHttpsCaller.getURLConnection(url);		
			SFHttpsCaller.setMethod(connection, SFHttpMethod.GET.toString());
			SFHttpsCaller.setAcceptLanguage(connection);
			SFHttpsCaller.addAuthenticationHeader(connection,mApiClient.getOAuthToken(),mUsername,mPassword,mCookieManager);
			
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
				
				int length;
				
				while ((length = fis.read(buffer)) > 0) 
				{
					if ( isCanceled() ) break;
					
					mOutputStream.write(buffer, 0, length);
					bytesRead+= length;
					updateProgress(bytesRead);
				}				
			}
            /*
			else if(httpErrorCode == HttpsURLConnection.HTTP_NO_CONTENT)
			{
				
			}*/
			else
			{
				responseString = SFHttpsCaller.readErrorResponse(connection);
                Logger.d(TAG,"Error " + responseString);
			}
				    									
		}
		finally {
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
				Logger.e(TAG,e);
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
				SFNotAuthorizedException v3Error = new SFNotAuthorizedException("Unauthorized (401)");
				mResponse.setFields(HttpsURLConnection.HTTP_UNAUTHORIZED, v3Error,downloadedBytes);
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
            SFV3ErrorParser parser = new SFV3ErrorParser(httpCode,responseString,null);
			SFServerException v3Error = new SFServerException(httpCode, parser.errorDisplayString());
			mResponse.setFields(httpCode, v3Error,downloadedBytes);
		} 
		catch (Exception e)  
		{
			callInternalErrorResponseFiller(e,downloadedBytes);
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
			Logger.d(TAG, "exception in updateProgress" , e);
		}		
	}
	
	
	/**
	 *   This is a filler only. wont do any parsing.
	 */
	private void callInternalErrorResponseFiller(Exception e,long bytesDownloaded)
	{
		SFOtherException v3Error = new SFOtherException(e);
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