package com.sharefile.api.https;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sharefile.api.SFApiClient;
import com.sharefile.api.SFSDKDefaultAccessScope;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.gson.SFGsonHelper;
import com.sharefile.api.interfaces.SFApiUploadProgressListener;
import com.sharefile.api.models.SFUploadSpecification;
import com.sharefile.java.log.SLog;

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
	private static final String TAG = SFKeywords.TAG + "-upload";
	
	private final SFUploadSpecification mUploadSpecification;
	private final long mResumeFromByteIndex;
	private final long mTotalBytes;
	private final InputStream mFileInputStream;
	private final SFApiClient mApiClient;
	private final SFApiUploadProgressListener mProgressListener;
	private final String mDestinationFileName;	
	private final SFCookieManager mCookieManager;
	//credntials for connectors
	private final String mUsername;
	private final String mPassword;
	private final String mDetails;
	
	public SFApiFileUploadRunnable(SFUploadSpecification uploadSpecification,
									 int resumeFromByteIndex, 
									 long tolalBytes,
									 String destinationName,
									 InputStream inputStream, 									 
									 SFApiClient client,
									 SFApiUploadProgressListener progressListener,
									 SFCookieManager cookieManager,String connUserName,String connPassword, String details) 
	{		
		mUploadSpecification = uploadSpecification;
		mResumeFromByteIndex = resumeFromByteIndex;
		mDestinationFileName = destinationName;
		mTotalBytes = tolalBytes;
		mFileInputStream = inputStream;
		mApiClient = client;
		mProgressListener = progressListener;
		mCookieManager = cookieManager;
		mUsername = connUserName;
		mPassword = connPassword;
		mDetails = details;
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
			SLog.d(TAG, "Seek exception" , e);
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
		
		if(isbatchLast && mDetails!=null && mDetails.length()>0)
		{
			try 
			{
				sb.append("&details="+URLEncoder.encode(mDetails,SFKeywords.UTF_8));
			} 
			catch (UnsupportedEncodingException e) 
			{				
				SLog.e(TAG,e);
			}
		}
		
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
	
	/**
	 * Chunk upload response can be of json type sample:
	 * 
	 * Sample error message {"error":true,"errorMessage":"Thread was being aborted.","errorCode":420}
	 * 
	 * Sample upload response V1: {"error":false,"value":[{"uploadid":"bbd75cd7-8038-4499-bab0-dc4a0522f0ac","parentid":"fo4f74b1-902f-4728-bb30-716851531beb","id":"fi627e76-a97c-4c4e-2e19-67fc65e97597","filename":"20140614_202946.jpg","displayname":"20140614_202946.jpg","size":1048576,"md5":"ea622e415c18add6a229faed8c48912d"}]}
	 * Sample upload response V3: {"error":false,"value":[{"id":"4L24TVJSEz6Ca22LWoZg44MrInfeT8lRuNA6WtqMyJE_","filename":"","displayname":"","size":267943,"md5":"6f42bdabb534dfb4d5ee6ea6cef22d5b"}]}	 
	 */
	public static class SFChunkUploadResponse
	{
		public boolean mWasError;
		public int mErrorCode;
		public String mErrorMessage;
		public int mBytesTransferedInChunk;		
								
		@SFSDKDefaultAccessScope SFChunkUploadResponse(String jsonString)
		{												
			try 
			{
				/*
				JSONObject errorObject;
				errorObject = new JSONObject(jsonString);
				mWasError =  errorObject.optBoolean("error");	
				if(mWasError)
				{
					mErrorMessage = errorObject.optString("errorMessage");			
					mErrorCode = errorObject.optInt("errorCode");
					SLog.d(TAG, "Parsed Chunk response: " + mErrorMessage);
				}
				else
				{
					String value = errorObject.optString("value");
					SLog.d(TAG, "Parsed Chunk response: value = " + value);
				}*/
				
				JsonParser jsonParser = new JsonParser();
				JsonElement jsonElement = jsonParser.parse(jsonString);
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				
				mWasError =  SFGsonHelper.getBoolean(jsonObject, "error", false);	
				
				if(mWasError)
				{
					mErrorMessage = SFGsonHelper.getString(jsonObject, "errorMessage", "");			
					mErrorCode = SFGsonHelper.getInt(jsonObject, "errorCode", 0);
					SLog.d(TAG, "Parsed Chunk response: " + mErrorMessage);
				}
				else
				{
					String value = SFGsonHelper.getString(jsonObject, "value", "");
					SLog.d(TAG, "Parsed Chunk response: value = " + value);
				}
				
			} 
			catch (Exception e) 
			{				
				SLog.e(TAG,"exception parsing upload response",e);
				mWasError = true;
				mErrorMessage = "exception parsing upload response";
				mErrorCode = SFSDK.INTERNAL_HTTP_ERROR;				
			}						
		}
					
		@SFSDKDefaultAccessScope SFChunkUploadResponse(String otherError,int httpErroCode)
		{						
			mWasError =  true;			
			mErrorMessage = otherError;			
			mErrorCode = httpErroCode;									
		}
	}
	
	/**
	 *   This tries to upload a chunk. Returns a detialed object with the httpErrorCode and the ChunkResponse from the server.
	 *   ChunkResonse will never be null. In case of http errors or exceptions we fill the chunk response with https err response string.
	 */
	private SFAPiUploadResponse uploadChunk(byte[] fileChunk,int chunkLength,boolean isLast, MessageDigest md, long previousChunkTotal) throws Exception
	{
		long bytesUploaded = 0;
		HttpsURLConnection conn = null;	
		String responseString = null;
		int httpErrorCode = SFSDK.INTERNAL_HTTP_ERROR;
		
		SFAPiUploadResponse ret = new SFAPiUploadResponse();
		
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
			SFHttpsCaller.addAuthenticationHeader(conn, mApiClient.getOAuthToken(), mUsername,mPassword,mCookieManager);										
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
				updateProgress(bytesUploaded+previousChunkTotal);
			}
					
			poster.close();
			
			httpErrorCode = SFHttpsCaller.safeGetResponseCode(conn);			
			
			SFHttpsCaller.getAndStoreCookies(conn, new URL(finalURL),mCookieManager);
		    
			if(httpErrorCode == HttpsURLConnection.HTTP_OK)
			{														
				responseString = SFHttpsCaller.readResponse(conn);		
				SLog.d(TAG, "Upload Response: " + responseString);
				
				SFChunkUploadResponse chunkResonse = new SFChunkUploadResponse(responseString);
				if(!chunkResonse.mWasError)
				{
					chunkResonse.mBytesTransferedInChunk = (int) bytesUploaded;
				}
				ret.setFeilds(httpErrorCode, null, chunkResonse,bytesUploaded);
			}			
			else
			{
				responseString = SFHttpsCaller.readErrorResponse(conn);		
				SLog.d(TAG, "Upload Response: " + responseString);
				SFChunkUploadResponse chunkResonse = new SFChunkUploadResponse(responseString,httpErrorCode);				
				ret.setFeilds(httpErrorCode, null, chunkResonse,bytesUploaded);
			}						
		}	
		catch(Exception ex)
		{
			SLog.e(TAG,"chunk", ex);
			SFChunkUploadResponse chunkResonse = new SFChunkUploadResponse(ex.getLocalizedMessage(),SFSDK.INTERNAL_HTTP_ERROR);				
			ret.setFeilds(SFSDK.INTERNAL_HTTP_ERROR,ex.getLocalizedMessage(), chunkResonse,bytesUploaded);
		}
		finally
		{
			SFHttpsCaller.disconnect(conn);
		}
				
		return ret;
	}
	
	public void upload()
	{		
		String responseString = null;
		long bytesRead = mResumeFromByteIndex;
		int chunkSize = 1024*1024;		
		long previousChunkTotalBytes = mResumeFromByteIndex;
		SFAPiUploadResponse uploadResponse = null;
		
		try
		{										
			SLog.d(TAG, "POST " + mUploadSpecification.getChunkUri());
			
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
					SLog.d(TAG,"Chunk < 0: " + chunkLength);
					
					done = true;
					break;
				}
									
				boolean isLast = (mFileInputStream.available() ==0) ? true : false;
																		
				if(isLast)
				{
					SLog.d(TAG,"isLast = true");
					done = true;
				}
				
				uploadResponse  =  uploadChunk(fileChunk,chunkLength,isLast,md,previousChunkTotalBytes);
				
				//Note here we can rely on the 	uploadResponse.mChunkUploadResponse.mWasError to decide the succuess or failure.			
				if(uploadResponse.mChunkUploadResponse.mWasError == false)
				{
					if(uploadResponse.mChunkUploadResponse.mBytesTransferedInChunk > 0)
					{
						previousChunkTotalBytes+= uploadResponse.mChunkUploadResponse.mBytesTransferedInChunk;
					}
				}
				else
				{					
					SLog.d(TAG,"break");
					break;
				}
			}											
		}
		catch(Exception ex)
		{					
			responseString = "\nExceptionStack = " +ex.getStackTrace().toString();	
			uploadResponse = new SFAPiUploadResponse();
			uploadResponse.setFeilds(SFSDK.INTERNAL_HTTP_ERROR, responseString, null, previousChunkTotalBytes);
		}
		finally
		{			
			closeStream(mFileInputStream);			
		}
								
		callResponseListeners(uploadResponse);

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
	 *   This object will get filled with an errorCoode and the SFChunkUploadResponse . Http 200 doesn not necessarily mean 
	 *   upload success. Listeners should use the SFChunkUploadResponse to figure out final state of the upload operation.
	 *   the httpErrorCode and be used to decide retries etc.
	 */
	public static class SFAPiUploadResponse
	{
		public int mHttpErrorCode = SFSDK.INTERNAL_HTTP_ERROR;
		public String mExtraMessae = "internal error";
		public SFChunkUploadResponse mChunkUploadResponse=null;			
		public long mTotalBytesUploadedTillNow = 0;
				
		public void setFeilds(int httpErrorCode,String extraMessage, SFChunkUploadResponse respnonse, long totalBytesUploaded)
		{
			mHttpErrorCode = httpErrorCode;
			mExtraMessae = extraMessage;
			mChunkUploadResponse = respnonse;						
			mTotalBytesUploadedTillNow = totalBytesUploaded;
		}
	};
					
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
			SLog.d(TAG, "exception update progress", e);
		}		
	}
	
	private void callResponseListeners(SFAPiUploadResponse uploadResponse)
	{
		if(mProgressListener == null)
		{
			return;
		}
		
		try
		{
			if(!uploadResponse.mChunkUploadResponse.mWasError)
			{				
				mProgressListener.uploadSuccess(uploadResponse.mTotalBytesUploadedTillNow, mUploadSpecification, mApiClient);				
			}
			else
			{		
				mProgressListener.uploadFailure(uploadResponse, mUploadSpecification, mApiClient);								
			}
		}
		catch(Exception ex)
		{
			SLog.d(TAG, "!!Exception calling the responseListener : ",ex);
		}
	}
			
	public Thread startNewThread()
	{
		Thread sfApithread = new Thread(this);		
		sfApithread.start();
		return sfApithread;
	}	
}