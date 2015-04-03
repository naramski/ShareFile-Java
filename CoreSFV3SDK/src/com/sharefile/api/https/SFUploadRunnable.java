package com.sharefile.api.https;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sharefile.api.SFApiClient;
import com.sharefile.api.SFQueryBuilder;
import com.sharefile.api.SFSDKDefaultAccessScope;

import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSdkGlobals;
import com.sharefile.api.enumerations.SFSafeEnum;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFNotAuthorizedException;
import com.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.sharefile.api.exceptions.SFOtherException;
import com.sharefile.api.exceptions.SFServerException;
import com.sharefile.api.gson.SFGsonHelper;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.models.SFUploadMethod;
import com.sharefile.api.models.SFUploadSpecification;
import com.sharefile.api.log.Logger;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

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
public class SFUploadRunnable extends TransferRunnable  
{	
	private static final String TAG = "SFUploadRunnable";
	
	private final long mResumeFromByteIndex;
	private final long mTotalBytes;
	private final InputStream mFileInputStream;
	private final String mDestinationFileName;

	private final String mDetails;
	
//	private final String mParentId;
	private final String mV3Url;
	private final boolean mOverwrite;

	private SFUploadSpecification mUploadSpecification;
	private SFChunkUploadResponse mChunkUploadResponse=null;

	public SFUploadRunnable(
		String v3Url, boolean overwrite,
		int resumeFromByteIndex, long tolalBytes, String destinationName,
		InputStream inputStream, SFApiClient client, IProgress progressListener,
		SFCookieManager cookieManager,String connUserName,String connPassword, String details
	) {
        super(client,progressListener,cookieManager,connUserName,connPassword);
		mResumeFromByteIndex = resumeFromByteIndex;
		mDestinationFileName = destinationName;
		mTotalBytes = tolalBytes;
		mFileInputStream = inputStream;
		mDetails = details;
		
		// mParentId = parentId;
		mV3Url = v3Url;
		mOverwrite = overwrite;
	}

	protected Result runInThisThread() {
		try {
			// get spec
			mUploadSpecification = getSpecification();
			if ( cancelRequested.get() ) {
				// create "cancel error" regardless of result
				return createCancelResult(0);
			}
			
			// upload
			Result result = upload();
			if ( cancelRequested.get() ) {
				// create "cancel error" regardless of result
				return createCancelResult(result.getBytesTransfered());
			}
			return result;
			
		} catch (SFServerException e) {
			Logger.e(TAG, e);
			Result ret = new Result();
			ret.setFields(SFSdkGlobals.INTERNAL_HTTP_ERROR, e, 0);
			return ret;
			
		} catch(Exception e) {		
			Logger.e(TAG, e);
			SFOtherException v3Error = new SFOtherException(e);
			Result ret = new Result();
			ret.setFields(SFSdkGlobals.INTERNAL_HTTP_ERROR, v3Error, 0 /*?????*/);
			return ret;
			
		}
	}

	private SFUploadSpecification getSpecification() throws SFInvalidStateException,
            SFServerException,
            SFNotAuthorizedException,
            SFOAuthTokenRenewException,
            SFOtherException
    {
		try 
		{
            Date now = new Date();

            ISFQuery<SFUploadSpecification> uploadQuery = SFQueryBuilder.ITEMS.upload(new URI(mV3Url)
                            ,new SFSafeEnum<SFUploadMethod>(SFUploadMethod.Streamed),
                            true,
                            mDestinationFileName,
                            mTotalBytes,
                            null,
                            false,
                            true,
                            false,
                            false,
                            "sfsdk",
                            mOverwrite,
                            mDestinationFileName,
                            mDetails,
                            false,
                            null,
                            null,
                            1,
                            "json",
                            false, now,now, 5*365);

			// uploadQuery.setLink(mV3Url);


            uploadQuery.setCredentials(mUsername,mPassword);

            return mApiClient.executeQuery(uploadQuery);
		}  
		catch (URISyntaxException e)  
		{				
			Logger.e(TAG, e);
		}


		return null;
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
			Logger.d(TAG, "Seek exception" , e);
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
                      Logger.e(TAG,e);
                }
          }
         
          return sb.toString();
    }
	
	public static String md5ToString(MessageDigest md) 
	{
		StringBuilder hash = new StringBuilder();
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
	 * //Sample error message {"error":true,"errorMessage":"Thread was being aborted.","errorCode":420}
	 * 
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
					Logger.d(TAG, "Parsed Chunk response: " + mErrorMessage);
				}
				else
				{
					String value = errorObject.optString("value");
					Logger.d(TAG, "Parsed Chunk response: value = " + value);
				}*/
				
				JsonParser jsonParser = new JsonParser();
				JsonElement jsonElement = jsonParser.parse(jsonString);
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				
				mWasError =  SFGsonHelper.getBoolean(jsonObject, "error", false);	
				
				if(mWasError)
				{
					mErrorMessage = SFGsonHelper.getString(jsonObject, "errorMessage", "");			
					mErrorCode = SFGsonHelper.getInt(jsonObject, "errorCode", 0);
					Logger.d(TAG, "Parsed Chunk response: " + mErrorMessage);
				}
				else
				{
					String value = SFGsonHelper.getString(jsonObject, "value", "");
					Logger.d(TAG, "Parsed Chunk response: value = " + value);
				}
				
			} 
			catch (Exception e) 
			{				
				Logger.e(TAG,"exception parsing upload response",e);
				mWasError = true;
				mErrorMessage = "exception parsing upload response";
				mErrorCode = SFSdkGlobals.INTERNAL_HTTP_ERROR;
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
	private Result uploadChunk(byte[] fileChunk,int chunkLength,boolean isLast, MessageDigest md, long previousChunkTotal) throws Exception
	{
		long bytesUploaded = 0;
		HttpsURLConnection conn = null;	
		String responseString = null;
		int httpErrorCode;
		
		Result ret = new Result();
		
		try
		{			
			//md5 hash buffer
			md.update(fileChunk, 0, chunkLength);
			
			//you need the RAW param or you'll have to do HTTP multi-part post...
			String append = getAppendParams(mDestinationFileName, mTotalBytes,isLast?1:0, isLast, md5ToString(md));
			final String finalURL = mUploadSpecification.getChunkUri() + append;

			conn = (HttpsURLConnection)(new URL(finalURL)).openConnection();					
			SFHttpsCaller.addAuthenticationHeader(conn, mApiClient.getOAuthToken(), mUsername,mPassword,mCookieManager);										
			conn.setUseCaches(false);
			conn.setRequestProperty(SFKeywords.CONTENT_TYPE, SFKeywords.APPLICATION_OCTET_STREAM);															
			conn.setRequestProperty(SFKeywords.CONTENT_LENGTH, ""+chunkLength);
			conn.setFixedLengthStreamingMode(chunkLength);
			SFHttpsCaller.setPostMethod(conn);
			conn.connect();
			
			//small buffer between the chunk and the stream so we can interrupt and kill task quickly
			final byte[] buffer = new byte[1024];
			final ByteArrayInputStream in = new ByteArrayInputStream(fileChunk,0,chunkLength);
			int currentBytesRead;
			OutputStream poster = new DataOutputStream(conn.getOutputStream());					
						
			int count = 0; 
			while((currentBytesRead = in.read(buffer,0,1024)) >0)
			{						
				poster.write(buffer,0,currentBytesRead);
				bytesUploaded+=(long)currentBytesRead;				
				poster.flush();//needs to be here				
				
				// onlu send notifications every 50kb
				if ( count++ % 50 == 0 ) updateProgress(bytesUploaded+previousChunkTotal);
				
				if (cancelRequested.get()) {
					// cancel
					return ret;
				}
			}
					
			poster.close();
			
			httpErrorCode = SFHttpsCaller.safeGetResponseCode(conn);			
			
			SFHttpsCaller.getAndStoreCookies(conn, new URL(finalURL),mCookieManager);
		    
			if(httpErrorCode == HttpsURLConnection.HTTP_OK)
			{														
				responseString = SFHttpsCaller.readResponse(conn);		
				Logger.d(TAG, "Upload Response: " + responseString);
				
				mChunkUploadResponse = new SFChunkUploadResponse(responseString);
				if(!mChunkUploadResponse.mWasError)
				{
					mChunkUploadResponse.mBytesTransferedInChunk = (int) bytesUploaded;
				}
				ret.setFields(httpErrorCode, null, bytesUploaded); 
			}			
			else
			{
				responseString = SFHttpsCaller.readErrorResponse(conn);		
				Logger.d(TAG, "Upload Response: " + responseString);
				mChunkUploadResponse = new SFChunkUploadResponse(responseString,httpErrorCode);
				ret.setFields(httpErrorCode, null, bytesUploaded);
			}
			
		} catch(Exception ex) {
			Logger.e(TAG,"chunk", ex);
			mChunkUploadResponse = new SFChunkUploadResponse(ex.getLocalizedMessage(), SFSdkGlobals.INTERNAL_HTTP_ERROR);
			SFOtherException v3Error = new SFOtherException(ex);
			ret.setFields(SFSdkGlobals.INTERNAL_HTTP_ERROR, v3Error, bytesUploaded);
			
		} finally {
			SFHttpsCaller.disconnect(conn);
		}
				
		return ret;
	}
	
	private Result upload() {
		String responseString;
//		long bytesRead = mResumeFromByteIndex;
		int chunkSize = 1024*1024;		
		long previousChunkTotalBytes = mResumeFromByteIndex;
		Result uploadResponse = null;
		
		try {										
			Logger.d(TAG, "POST " + mUploadSpecification.getChunkUri());
			
			seekInputStream();			
			int chunkLength;
			final MessageDigest md = MessageDigest.getInstance("MD5");						
			byte[] fileChunk = new byte[chunkSize];			
			boolean done = false;
			while(!done)  {													
				//fill chunk
				chunkLength = mFileInputStream.read(fileChunk, 0, fileChunk.length);
				if (chunkLength<0) {
					Logger.d(TAG,"Chunk < 0: " + chunkLength);
					done = true;
					break;
				}
									
				boolean isLast = (mFileInputStream.available() == 0);
				if(isLast) {
					Logger.d(TAG,"isLast = true");
					done = true;
				}
				
				uploadResponse  =  uploadChunk(fileChunk,chunkLength,isLast,md,previousChunkTotalBytes);

				if (cancelRequested.get()) {
					// cancel
					return uploadResponse;
				}

				//Note here we can rely on the 	uploadResponse.mChunkUploadResponse.mWasError to decide the succuess or failure.			
				if(mChunkUploadResponse.mWasError) {
					Logger.d(TAG, "Error uploading chunk - break");
                    uploadResponse = new Result();
                    SFServerException v3Error = new SFServerException(0,mChunkUploadResponse.mErrorMessage);
                    uploadResponse.setFields(mChunkUploadResponse.mErrorCode, v3Error, previousChunkTotalBytes);
					return uploadResponse;
				}					

				if(mChunkUploadResponse.mBytesTransferedInChunk > 0) {
					previousChunkTotalBytes+= mChunkUploadResponse.mBytesTransferedInChunk;
				}
				
				Thread.yield();
			}
			
		} catch(Exception ex) {
			uploadResponse = new Result();
			SFOtherException v3Error = new SFOtherException(ex);
			uploadResponse.setFields(SFSdkGlobals.INTERNAL_HTTP_ERROR, v3Error, previousChunkTotalBytes);
			
		} finally {			
			closeStream(mFileInputStream);			
		}
		
		return uploadResponse;
	}
	
	private void closeStream(Closeable fis) {
		if(fis==null) return;

		try {
			fis.close();
			
		} catch (IOException e)  {				
			Logger.e(TAG,e);
		}
	}
					
	private void updateProgress(long uploadedBytes)
	{
		if(mProgressListener == null) {
			return;
		}
		
		try
		{
			mProgressListener.bytesTransfered(uploadedBytes);
		}
		catch(Exception e)
		{
			Logger.d(TAG, "exception update progress", e);
		}		
	}			
}