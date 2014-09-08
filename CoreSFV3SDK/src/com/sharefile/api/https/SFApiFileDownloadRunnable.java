package com.sharefile.api.https;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import com.sharefile.api.SFApiClient;
import com.sharefile.api.SFV3Error;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.enumerations.SFHttpMethod;
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
	private final SFApiDownloadProgressListener mProgressListener;		
	private final SFCookieManager mCookieManager;
	
	//credentials for connectors
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
	public void run() 
	{
		executeBlockingQuery();
	}
	
	public void executeBlockingQuery()
	{
		int httpErrorCode =  SFSDK.INTERNAL_HTTP_ERROR;
		String responseString = null;
		long bytesRead = mResumeFromByteIndex;
		URLConnection connection = null;
		InputStream fis = null;
		SFV3Error sfV3Error = null;		
		
		try
		{										
			SLog.d(TAG, "GET " + mDownloadSpecification.getDownloadUrl());
			
			URL url = mDownloadSpecification.getDownloadUrl().toURL();
			connection = SFHttpsCaller.getURLConnection(url);		
			SFHttpsCaller.setMethod(connection, SFHttpMethod.GET.toString());
			mApiClient.getConfig().setAddtionalHeaders(connection);
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
				
				int length = 0;
				
				while ((length = fis.read(buffer)) > 0) 
				{
					mOutputStream.write(buffer, 0, length);
					bytesRead+= length;
					updateProgress(bytesRead);
				}								
			}			
			else
			{
				responseString = SFHttpsCaller.readErrorResponse(connection);
				sfV3Error = new SFV3Error(httpErrorCode,responseString,null);
			}				    								
		}
		catch(Exception ex)
		{					
			sfV3Error = new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR,null,ex);															
		}
		finally
		{
			closeStream(fis);
			closeStream(mOutputStream);
			SFHttpsCaller.disconnect(connection);
		}
								
		callResponseListeners(sfV3Error,bytesRead);
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
			SLog.e(TAG, "exception in updateProgress" , e);
		}		
	}
	
	private void callResponseListeners(SFV3Error sfv3Error, long bytesDownloaded)
	{
		if(mProgressListener == null)
		{
			return;
		}
		
		try
		{
			if(sfv3Error==null)
			{
					mProgressListener.downloadSuccess(bytesDownloaded, mDownloadSpecification, mApiClient);
			}
			else
			{
					mProgressListener.downloadFailure(sfv3Error,bytesDownloaded, mDownloadSpecification, mApiClient);
			}
		}
		catch(Exception ex)
		{
			SLog.d(TAG, "!!Exception calling the responseListener",ex);
		}
	}
		
	public Thread startNewThread()
	{
		Thread sfApithread = new Thread(this);		
		sfApithread.start();
		return sfApithread;
	}	
}