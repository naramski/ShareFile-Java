package com.sharefile.api.interfaces;

import com.sharefile.api.SFQueryStream;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFNotAuthorizedException;
import com.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.sharefile.api.exceptions.SFOtherException;
import com.sharefile.api.exceptions.SFServerException;
import com.sharefile.api.https.SFDownloadRunnable;
import com.sharefile.api.https.SFUploadRunnable;
import com.sharefile.api.https.TransferRunnable;
import com.sharefile.api.models.SFFile;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFODataObject;

import java.io.InputStream;
import java.io.OutputStream;

public interface ISFApiClient extends IOAuthTokenChangeHandler
{
    public <T extends SFODataObject> T executeQuery(ISFQuery<T> query)
            throws SFServerException, SFInvalidStateException,
            SFNotAuthorizedException, SFOAuthTokenRenewException,SFOtherException;

    public InputStream executeQuery(SFQueryStream query)
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
                                        TransferRunnable.IProgress progressListener)
            throws SFInvalidStateException, SFServerException;
}