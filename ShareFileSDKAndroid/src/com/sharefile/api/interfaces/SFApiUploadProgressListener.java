package com.sharefile.api.interfaces;

import com.sharefile.api.SFApiClient;
import com.sharefile.api.https.SFApiFileUploadRunnable.SFAPiUploadResponse;
import com.sharefile.api.models.SFUploadSpecification;

public interface SFApiUploadProgressListener 
{
	public void bytesUploaded(long byteCount,SFUploadSpecification uploadSpec,SFApiClient client);
	public void uploadSuccess(long byteCount,SFUploadSpecification uploadSpec,SFApiClient client);
	public void uploadFailure(SFAPiUploadResponse v3error,SFUploadSpecification uploadSpec,SFApiClient client);
}