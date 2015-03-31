package com.sharefile.api.async;

import com.sharefile.api.SFSDKDefaultAccessScope;
import com.sharefile.api.interfaces.ISFApiClient;
import com.sharefile.api.interfaces.ISFApiResultCallback;
import com.sharefile.api.interfaces.ISFAsyncTask;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.models.SFODataObject;

@SFSDKDefaultAccessScope
class SFAsyncTask implements ISFAsyncTask, Runnable
{
    private final SFAsyncHelper mAsyncHelper;

    public <T extends SFODataObject> SFAsyncTask(ISFApiClient apiClient, ISFQuery<T> query, ISFApiResultCallback<T> callback)
    {
        mAsyncHelper = new SFAsyncHelper(apiClient,query,callback);
    }

    @Override
    public void execute()
    {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run()
    {
        mAsyncHelper.doInBackground();
        mAsyncHelper.onPostExecute();
    }
}