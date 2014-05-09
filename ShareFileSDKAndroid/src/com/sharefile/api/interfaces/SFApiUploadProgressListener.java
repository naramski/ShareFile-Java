package com.sharefile.api.interfaces;

import com.sharefile.api.SFApiClient;
import com.sharefile.api.SFV3Error;
import com.sharefile.api.models.SFUploadSpecification;

public interface SFApiUploadProgressListener 
{
	public void bytesUploaded(long byteCount,SFUploadSpecification uploadSpec,SFApiClient client);
	public void uploadSuccess(long byteCount,SFUploadSpecification uploadSpec,SFApiClient client);
	public void uploadFailure(SFV3Error v3error,long byteCount,SFUploadSpecification uploadSpec,SFApiClient client);
}