package com.sharefile.api.https;

import com.sharefile.api.SFApiClient;

import com.sharefile.api.exceptions.SFSDKException;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class TransferRunnable extends Thread
{
    protected AtomicBoolean cancelRequested = new AtomicBoolean(false);
    protected final SFApiClient mApiClient;
    protected final IProgress mProgressListener;
    protected final SFCookieManager mCookieManager;
    protected long mTotalBytesTransferredForThisFile;

    //credentials for connectors
    protected final String mUsername;
    protected final String mPassword;

    TransferRunnable(SFApiClient client,IProgress progressListener, SFCookieManager cookieManager,
                     String userName,String password)
    {
        mApiClient = client;
        mProgressListener = progressListener;
        mCookieManager = cookieManager;
        mUsername = userName;
        mPassword = password;
    }

	public interface IProgress {
		public void bytesTransfered(long byteCount);
        public void onError(SFSDKException exception, long bytesTransfered);
        public void onComplete(long bytesTransfered);
	};


    protected abstract void runInThisThread() throws SFSDKException;

    /**
     * execute Transfer in this thread overriding the cancel signal
     * @param cancel
     * @return
     */
    public void runInThisThread(AtomicBoolean cancel) throws SFSDKException
    {
        cancelRequested = cancel;
        runInThisThread();
    }

    @Override
    public void run()
    {
        try
        {
            runInThisThread();
        }
        catch (SFSDKException e)
        {
            if(mProgressListener!=null)
            {
                mProgressListener.onError(e, mTotalBytesTransferredForThisFile);
            }
        }
    }


    public void cancel()
    {
        cancelRequested.set(true);
    }
}
