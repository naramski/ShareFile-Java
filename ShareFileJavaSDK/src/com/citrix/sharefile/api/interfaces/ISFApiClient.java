package com.citrix.sharefile.api.interfaces;

import com.citrix.sharefile.api.SFQueryStream;
import com.citrix.sharefile.api.entities.ISFEntities;
import com.citrix.sharefile.api.exceptions.SFInvalidStateException;
import com.citrix.sharefile.api.exceptions.SFNotAuthorizedException;
import com.citrix.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.citrix.sharefile.api.exceptions.SFOtherException;
import com.citrix.sharefile.api.exceptions.SFServerException;
import com.citrix.sharefile.api.extensions.SFCapabilitiesEntityEx;
import com.citrix.sharefile.api.https.SFDownloadRunnable;
import com.citrix.sharefile.api.https.upload.SFUploadRunnable;
import com.citrix.sharefile.api.https.TransferRunnable;
import com.citrix.sharefile.api.models.SFFile;
import com.citrix.sharefile.api.models.SFFolder;
import com.citrix.sharefile.api.models.SFODataObject;
import com.citrix.sharefile.api.models.SFUploadRequestParams;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

public interface ISFApiClient extends IOAuthTokenChangeHandler , ISFEntities
{
    public <T extends SFODataObject> T executeQuery(ISFQuery<T> query)
            throws SFServerException, SFInvalidStateException,
            SFNotAuthorizedException, SFOAuthTokenRenewException,SFOtherException;

    public InputStream executeQueryEx(SFQueryStream query)
            throws SFServerException, SFInvalidStateException,
            SFNotAuthorizedException, SFOAuthTokenRenewException,SFOtherException;

    public String getUserId();

    public <T> ISFApiExecuteQuery getExecutor(ISFQuery<T> query,
                                   ISFApiResultCallback<T> apiResultCallback,
                                   ISFReAuthHandler reAuthHandler)
            throws SFInvalidStateException;

    public SFDownloadRunnable getDownloader(SFFile file, OutputStream outputStream,
                                            TransferRunnable.IProgress progressListener)
            throws SFOtherException;

    public SFUploadRunnable getUploader(SFFolder parentFolder,
                                        String destinationName,
                                        String details,long fileSizeInBytes,
                                        InputStream inputStream,
                                        SFUploadRunnable.IUploadProgress progressListener)
            throws SFInvalidStateException, SFServerException;

    public SFUploadRunnable getUploader(SFUploadRequestParams uploadRequestParams,
                                        InputStream inputStream,
                                        SFUploadRunnable.IUploadProgress progressListener)
            throws SFInvalidStateException, SFServerException;

    public URI getDefaultUrl(String folderID) throws URISyntaxException;
    public URI getTopUrl();
    public URI getDeviceUrl(String deviceId) throws URISyntaxException;

    public void setReAuthHandler(ISFReAuthHandler reAuthHandler);

    public ISFApiClient clone();

    SFCapabilitiesEntityEx capabilitiesEx();

}