package com.sharefile.api.async;

import com.sharefile.api.SFV3Error;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFNotAuthorizedException;
import com.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.sharefile.api.exceptions.SFV3ErrorException;
import com.sharefile.api.interfaces.ISFApiClient;
import com.sharefile.api.interfaces.ISFApiExecuteQuery;
import com.sharefile.api.interfaces.ISFApiResultCallback;
import com.sharefile.api.interfaces.ISFQuery;

public class SFAsyncHelper<T>
{
    private final ISFApiClient mApiClient;
    private final ISFQuery<T> mQuery;
    private final ISFApiResultCallback<T> mApiResultCallback;
    private ISFApiExecuteQuery mApiExecutor;
    private T mResult;
    private Exception mException;

    public SFAsyncHelper(ISFApiClient apiClient, ISFQuery query, ISFApiResultCallback apiResultCallback)
    {
        this.mApiClient = apiClient;
        this.mQuery = query;
        this.mApiResultCallback = apiResultCallback;
    }

    public T doInBackground()
    {
        try
        {
            mApiExecutor = mApiClient.getExecutor(mQuery, mApiResultCallback, null);
            mResult = mApiExecutor.executeBlockingQuery();
        }
        catch (Exception e)
        {
            mException = e;
        }

        return mResult;
    }

    public void onPostExecute()
    {
        if(mException !=null)
        {
            if(mException instanceof SFV3ErrorException)
            {
                mApiResultCallback.onError(((SFV3ErrorException) mException).getV3Error(),mQuery);
                return;
            }

            SFV3Error sfv3Error = new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR, mException.getLocalizedMessage());
            mApiResultCallback.onError(sfv3Error,mQuery);

            return;
        }

        mApiResultCallback.onSuccess(mResult);
    }
}