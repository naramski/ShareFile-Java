package com.sharefile.api.https;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;

import javax.net.ssl.HttpsURLConnection;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sharefile.api.SFApiClient;
import com.sharefile.api.SFV3Error;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.gson.auto.SFDefaultGsonParser;
import com.sharefile.api.interfaces.SFApiUploadProgressListener;
import com.sharefile.api.models.SFUploadSpecification;
import com.sharefile.api.utils.SFLog;

/**
 * 
 *  {
  "Method": "Streamed",
  "ChunkUri": "https://storage-ec2-34.sharefile.com/upload-streaming-2.aspx?batchid=723ffb79-d720-4de8-b95d-087be1e6462a&overwrite=true&uploadid=rsu-9c48c04a5fb84f9d8fc93875231b01ca&raw=1&encparams=phT2SuROQY1XrVtmjxwO2yRkHK2uBy1BtAjWhNYAu-lvRQr3eTm-Xh6zYbz3Lc9FfRoDvzBCdI7SyPDVrxL0bR9-cb7IsTFbIcnPRRJ8xLn0nJ0yxIeKXV-m-gb0HWzHGdHOnKTBEEgwD969n-8rbXu6jvkZ3UhEwY6I79GfdTogsf1vi7lFrfPrTe8TV5isPOPSpSINN3qxBI_qWJoy8D_AUnK009vuFHJP4t9yzPi6rDqmfU-7ZhmhncG4rsBfTdEWL3wQiOdU5kNnFl5WkSzjO8dg1vmjSidVSreUN_YJeQw95i8w5XaVA7BGuWDDNXjE8rV53cyQ9p2blo6Xovj4fxQi6wSf0J6CqLKZnFgbHTd7IVvexhdXhTMYAWce01eMBr7iWD9Kd2J3O_nLX3oEWKMDgqCg6SCSd8baO9_EBiQ8QnDwfh8tWeOQq60_gXeX-wBn_TAPNUPTmV1RyD5nt9sn5Q77Rd6YtiOiSjNI98z2mCTSiLp3TYMm4gB38CawObllVnJuWIERHKGbyEwJhE5PNxdMQC4PeM4$",
  "IsResume": false,
  "ResumeIndex": 0,
  "ResumeOffset": 0,
  "ResumeFileHash": "",
  "odata.metadata": "https://nilesh.sf-api.com/sf/v3/$metadata#UploadSpecification/ShareFile.Api.Models.UploadSpecification@Element"
}
 */
public class SFApiFileUploadRunnable implements Runnable  
{	
	private static final String TAG = "-upload";
	
	private final SFUploadSpecification mUploadSpecification;
	private final long mResumeFromByteIndex;
	private final long mTotalBytes;
	private final InputStream mFileInputStream;
	private final SFApiClient mApiClient;
	private final SFApiUploadProgressListener mProgressListener;
	private final String mDestinationFileName;
	private FinalResponse mResponse = new FinalResponse();
	private final SFCookieManager mCookieManager;
	
	public SFApiFileUploadRunnable(SFUploadSpecification uploadSpecification,
									 int resumeFromByteIndex, 
									 long tolalBytes,
									 String destinationName,
									 InputStream inputStream, 									 
									 SFApiClient client,
									 SFApiUploadProgressListener progressListener,
									 SFCookieManager cookieManager) 
	{		
		mUploadSpecification = uploadSpecification;
		mResumeFromByteIndex = resumeFromByteIndex;
		mDestinationFileName = destinationName;
		mTotalBytes = tolalBytes;
		mFileInputStream = inputStream;
		mApiClient = client;
		mProgressListener = progressListener;
		mCookieManager = cookieManager;
	}

	@Override
	public void run() 
	{
		upload();
	}
	
	private void seekInputStream()
	{
		try
		{
			if(mResumeFromByteIndex > 0)
			{
				mFileInputStream.skip(mResumeFromByteIndex);
			}
		}
		catch(Exception e)
		{
			SFLog.d2(TAG, "Seek exception : %s" , Log.getStackTraceString(e));
		}
	}
	
	private String getAppendParams(String filename, long fileSize,int finish,boolean isbatchLast,String hash)
	{		
		StringBuilder sb = new StringBuilder();
		
		sb.append("&filehash="); sb.append(hash);
		sb.append("&finish="+ finish);
		if(isbatchLast)
		{
			sb.append("&isbatchlast=true"); 
		}
		sb.append("&fmt=json");
		sb.append("&hash="+hash);
		sb.append("&filesize="+fileSize);						
		
		String appendParam =  sb.toString();
		return appendParam;				
	}
	
	public static String md5ToString(MessageDigest md) 
	{
		StringBuffer hash = new StringBuffer();
		byte digest[] = md.digest();
		String hex;
		for(byte part : digest) {
			hex = Integer.toHexString(0xff & part);
			if(hex.length() == 1)
				hash.append("0");
			hash.append(hex);
		}
		return hash.toString();
	}
	
	
	private long uploadChunk(byte[] fileChunk,int chunkLength,boolean isLast, MessageDigest md) throws Exception
	{
		long bytesUploaded = 0;
		HttpsURLConnection conn = null;	
		String responseString = null;
		int httpErrorCode = SFSDK.INTERNAL_HTTP_ERROR;
		
		try
		{			
			//md5 hash buffer
			md.update(fileChunk, 0, chunkLength);
			
			//you need the RAW param or you'll have to do HTTP multi-part post...
			String append = getAppendParams(mDestinationFileName, mTotalBytes,isLast?1:0, isLast?true:false, md5ToString(md));
			StringBuilder url = new StringBuilder();
			url.append(mUploadSpecification.getChunkUri() + append);																
														
			final String finalURL = url.toString();
				
			conn = (HttpsURLConnection)(new URL(finalURL)).openConnection();					
			SFHttpsCaller.addAuthenticationHeader(conn, mApiClient.getAuthToken(), null,null,mCookieManager);										
			conn.setUseCaches(false);
			conn.setRequestProperty(SFKeywords.CONTENT_TYPE, SFKeywords.APPLICATION_OCTET_STREAM);															
			conn.setRequestProperty(SFKeywords.CONTENT_LENGTH, ""+chunkLength);
			((HttpsURLConnection) conn).setFixedLengthStreamingMode(chunkLength);					
			SFHttpsCaller.setPostMethod(conn);
			conn.connect();
			
			//small buffer between the chunk and the stream so we can interrupt and kill task quickly
			final byte[] buffer = new byte[1024];
			final ByteArrayInputStream in = new ByteArrayInputStream(fileChunk,0,chunkLength);
			int currentBytesRead = 0;					
			OutputStream poster = new DataOutputStream(conn.getOutputStream());					
						
			while((currentBytesRead = in.read(buffer,0,1024)) >0)
			{						
				poster.write(buffer,0,currentBytesRead);
				bytesUploaded+=(long)currentBytesRead;				
				poster.flush();//needs to be here
				SFLog.v("ShareFile-Upload","k-bytesUploaded bytes =   " + bytesUploaded);
				updateProgress(bytesUploaded);
			}
					
			poster.close();
			
			httpErrorCode = SFHttpsCaller.safeGetResponseCode(conn);			
			
			SFHttpsCaller.getAndStoreCookies(conn, new URL(finalURL),mCookieManager);
		    
			if(httpErrorCode == HttpsURLConnection.HTTP_OK)
			{														
				responseString = SFHttpsCaller.readResponse(conn);				
			}
			else if(httpErrorCode == HttpsURLConnection.HTTP_NO_CONTENT)
			{
				
			}
			else
			{
				responseString = SFHttpsCaller.readErrorResponse(conn);		
				return -1;
			}						
		}		
		finally
		{
			SFHttpsCaller.disconnect(conn);
		}
				
		return bytesUploaded;
	}
	
	public void upload()
	{
		int httpErrorCode =  SFSDK.INTERNAL_HTTP_ERROR;
		String responseString = null;
		long bytesRead = mResumeFromByteIndex;
		int chunkSize = 1024*1024;
		long bytesUploaded = mResumeFromByteIndex;
		
		try
		{										
			SFLog.d2(TAG, "POST %s" , mUploadSpecification.getChunkUri());
			
			seekInputStream();			
			int chunkLength = 0;
			final MessageDigest md = MessageDigest.getInstance("MD5");						
			byte[] fileChunk = new byte[chunkSize];			
			boolean done = false;
			while(!done) 
			{													
				//fill chunk
				chunkLength = mFileInputStream.read(fileChunk, 0, fileChunk.length);
				
				if(chunkLength<0)
				{
					done = true;
					break;
				}
									
				boolean isLast = (mFileInputStream.available() ==0) ? true : false;
																		
				if(isLast)
				{
					done = true;
				}
				
				bytesUploaded += uploadChunk(fileChunk,chunkLength,isLast,md);
			}		
			
			httpErrorCode = HttpsURLConnection.HTTP_OK;
			mResponse.setFeilds(httpErrorCode, null,bytesUploaded);
		}
		catch(Exception ex)
		{		
			httpErrorCode = SFSDK.INTERNAL_HTTP_ERROR;
			responseString = "OrignalHttpCode = " + httpErrorCode + "\nExceptionStack = " +Log.getStackTraceString(ex);												
		}
		finally
		{			
			closeStream(mFileInputStream);			
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
		private SFV3Error mV3Error = null;			
		private long mBytesUploaded = 0;
		
		public void setFeilds(int errorCode, SFV3Error v3Error, long uploaded)
		{
			mHttpErrorCode = errorCode;
			mV3Error = v3Error;			
			mBytesUploaded = uploaded;
		}
	};
	
	/**
	 *   Parse the response to the best of our ability. At the end of this function the FinalResponse object 
	 *   has to be filled with an ErrorCode or HTTP_OK and the V3Error or SFOBject should be filled based on success or failure or 
	 *   response parsing.	 
	 */
	private void parseResponse(int httpCode,String responseString,long uploadedBytes)
	{
		switch(httpCode)
		{
			case HttpsURLConnection.HTTP_OK:
				mResponse.setFeilds(HttpsURLConnection.HTTP_OK, null,uploadedBytes);
			break;	
			
			case HttpsURLConnection.HTTP_NO_CONTENT:
				mResponse.setFeilds(HttpsURLConnection.HTTP_NO_CONTENT, null,uploadedBytes);
			break;
			
			case HttpsURLConnection.HTTP_UNAUTHORIZED:
				SFV3Error v3Error = new SFV3Error(httpCode,null,responseString);
				mResponse.setFeilds(HttpsURLConnection.HTTP_UNAUTHORIZED, v3Error,uploadedBytes);
			break;
			
			case SFSDK.INTERNAL_HTTP_ERROR:
				callInternalErrorResponseFiller(httpCode, responseString,null,uploadedBytes);
			break;
			
			default:
				callFailureResponseParser(httpCode, responseString,uploadedBytes);
			break;				
		}				
	}
	
	private void callFailureResponseParser(int httpCode, String responseString,long uploadedBytes)
	{													
		try 
		{
			JsonParser jsonParser = new JsonParser();
			JsonElement jsonElement =jsonParser.parse(responseString);				
			SFV3Error v3Error = SFDefaultGsonParser.parse(jsonElement);
			v3Error.httpResponseCode = httpCode;				
			mResponse.setFeilds(httpCode, v3Error,uploadedBytes);
		} 
		catch (Exception e)  
		{					
			/* 
			 * Note how we fill the httpErrorcode to httpCode. Thats coz the server originally returned it, 
			 * just the error object was malformed or caused some other exception while parsing.			 
			 */						
			callInternalErrorResponseFiller(httpCode,Log.getStackTraceString(e),responseString,uploadedBytes);
		}
	}
	
	private void updateProgress(long uploadedBytes)
	{
		if(mProgressListener == null)
		{
			return;
		}
		
		try
		{
			mProgressListener.bytesUploaded(uploadedBytes, mUploadSpecification, mApiClient);
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
					mProgressListener.uploadSuccess(mResponse.mBytesUploaded, mUploadSpecification, mApiClient);				
				break;	
				
				default:
					mProgressListener.uploadFailure(mResponse.mV3Error,mResponse.mBytesUploaded, mUploadSpecification, mApiClient);
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
	private void callInternalErrorResponseFiller(int httpCode,String errorDetails,String extraInfo,long bytesUploaded)
	{
		SFV3Error v3Error = new SFV3Error(httpCode,errorDetails,extraInfo);
		mResponse.setFeilds(SFSDK.INTERNAL_HTTP_ERROR, v3Error,bytesUploaded);
	}
	
	public Thread startNewThread()
	{
		Thread sfApithread = new Thread(this);		
		sfApithread.start();
		return sfApithread;
	}	
}