package com.sharefile.api.https;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sharefile.api.SFApiClient;
import com.sharefile.api.V3Error;
import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.gson.auto.SFDefaultGsonParser;
import com.sharefile.api.interfaces.SFApiDownloadProgressListener;
import com.sharefile.api.models.SFDownloadSpecification;

public class SFApiFileDownloadRunnable implements Runnable  
{	
	private static final String TAG = "-download";
	
	private final SFDownloadSpecification mDownloadSpecification;
	private final long mResumeFromByteIndex;
	private final FileOutputStream mFileOutputStream;
	private final SFApiClient mApiClient;
	private final SFApiDownloadProgressListener mProgressListener;
	private FinalResponse mResponse = new FinalResponse();	
	
	public SFApiFileDownloadRunnable(SFDownloadSpecification downloadSpecification,
									 int resumeFromByteIndex, 
									 FileOutputStream fileOutpuStream, 
									 SFApiClient client,
									 SFApiDownloadProgressListener progressListener) 
	{		
		mDownloadSpecification = downloadSpecification;
		mResumeFromByteIndex = resumeFromByteIndex;
		mFileOutputStream = fileOutpuStream;
		mApiClient = client;
		mProgressListener = progressListener;
	}

	@Override
	public void run() 
	{
		download();
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
			SFLog.d2(TAG, "GET %s" , mDownloadSpecification.getDownloadUrl());
			
			URL url = mDownloadSpecification.getDownloadUrl().toURL();
			connection = SFHttpsCaller.getURLConnection(url);		
			SFHttpsCaller.setMethod(connection, SFHttpMethod.GET.toString());
			SFHttpsCaller.setAcceptLanguage(connection);
			SFHttpsCaller.addAuthenticationHeader(connection,mApiClient.getAuthToken(),null,null);
			
			if(mResumeFromByteIndex!=0)
			{
				connection.setRequestProperty(SFKeywords.Range, "bytes="+mResumeFromByteIndex+"-");
			}
																				
			connection.connect();
			
			httpErrorCode = SFHttpsCaller.safeGetResponseCode(connection);			
						
			SFHttpsCaller.getAndStoreCookies(connection, url);
		    
			if(httpErrorCode == HttpsURLConnection.HTTP_OK)
			{														
				fis = connection.getInputStream();
				
				byte[] buffer = new byte[1024 * 1024];
				
				int length = 0;
				
				while ((length = fis.read(buffer)) > 0) 
				{
					mFileOutputStream.write(buffer, 0, length);
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
			closeStream(mFileOutputStream);
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
				e.printStackTrace();
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
		private V3Error mV3Error = null;			
		private long mBytesDownloaded = 0;
		
		public void setFeilds(int errorCode, V3Error v3Error, long downloaded)
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
		switch(httpCode)
		{
			case HttpsURLConnection.HTTP_OK:
				mResponse.setFeilds(HttpsURLConnection.HTTP_OK, null,downloadedBytes);
			break;	
			
			case HttpsURLConnection.HTTP_NO_CONTENT:
				mResponse.setFeilds(HttpsURLConnection.HTTP_NO_CONTENT, null,downloadedBytes);
			break;
			
			case HttpsURLConnection.HTTP_UNAUTHORIZED:
				V3Error v3Error = new V3Error(httpCode,null,responseString);
				mResponse.setFeilds(HttpsURLConnection.HTTP_UNAUTHORIZED, v3Error,downloadedBytes);
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
			V3Error v3Error = SFDefaultGsonParser.parse(jsonElement);
			v3Error.httpResponseCode = httpCode;				
			mResponse.setFeilds(httpCode, v3Error,downloadedBytes);
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
			SFLog.d2(TAG, "%s", Log.getStackTraceString(e));
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
			switch(mResponse.mHttpErrorCode)
			{
				case HttpsURLConnection.HTTP_OK:
					mProgressListener.downloadSuccess(mResponse.mBytesDownloaded, mDownloadSpecification, mApiClient);				
				break;	
				
				case HttpsURLConnection.HTTP_NO_CONTENT:
					//TODO: is this correct for a download?
					mProgressListener.downloadSuccess(mResponse.mBytesDownloaded, mDownloadSpecification, mApiClient);
				break;
												
				default:
					mProgressListener.downloadFailure(mResponse.mV3Error,mResponse.mBytesDownloaded, mDownloadSpecification, mApiClient);
				break;				
			}
		}
		catch(Exception ex)
		{
			SFLog.d2("-callback", "!!Exception calling the responseListener : %s ",Log.getStackTraceString(ex));
		}
	}
	
	/**
	 *   This is a filler only. wont do any parsing.
	 */
	private void callInternalErrorResponseFiller(int httpCode,String errorDetails,String extraInfo,long bytesDownloaded)
	{
		V3Error v3Error = new V3Error(httpCode,errorDetails,extraInfo);
		mResponse.setFeilds(SFSDK.INTERNAL_HTTP_ERROR, v3Error,bytesDownloaded);
	}
	
	public Thread startNewThread()
	{
		Thread sfApithread = new Thread(this);		
		sfApithread.start();
		return sfApithread;
	}	
}