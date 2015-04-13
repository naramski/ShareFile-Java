package com.sharefile.api.async;


import com.sharefile.api.SFSdk;
import com.sharefile.api.exceptions.SFSDKException;
import com.sharefile.api.interfaces.ISFApiClient;
import com.sharefile.api.interfaces.ISFApiExecuteQuery;
import com.sharefile.api.interfaces.ISFApiResultCallback;
import com.sharefile.api.interfaces.ISFAsyncHelper;
import com.sharefile.api.interfaces.ISFQuery;


public class SFAsyncHelper<T> implements ISFAsyncHelper<T>
{
    private final ISFApiClient mApiClient;
    private final ISFQuery<T> mQuery;
    private final ISFApiResultCallback<T> mApiResultCallback;
    private ISFApiExecuteQuery mApiExecutor;
    private T mResult;
    private SFSDKException mException;

    public SFAsyncHelper(ISFApiClient apiClient, ISFQuery query, ISFApiResultCallback apiResultCallback)
    {
        this.mApiClient = apiClient;
        this.mQuery = query;
        this.mApiResultCallback = apiResultCallback;
    }

    @Override
    public T execute()
    {
        try
        {
            mApiExecutor = mApiClient.getExecutor(mQuery, mApiResultCallback, SFSdk.getReAuthHandler());
            mResult = mApiExecutor.executeBlockingQuery();
        }
        catch (SFSDKException e)
        {
            mException = e;
        }

        return mResult;
    }

    @Override
    public void onPostExecute()
    {
        if(mException !=null)
        {
            mApiResultCallback.onError(mException,mQuery);
            return;
        }

        mApiResultCallback.onSuccess(mResult);
    }
}