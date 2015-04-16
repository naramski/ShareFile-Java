package com.sharefile.api.async;

import com.sharefile.api.SFSDKDefaultAccessScope;
import com.sharefile.api.interfaces.ISFApiClient;
import com.sharefile.api.interfaces.ISFApiResultCallback;
import com.sharefile.api.interfaces.ISFAsyncHelper;
import com.sharefile.api.interfaces.ISFAsyncTask;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.models.SFODataObject;

@SFSDKDefaultAccessScope
class SFDefaultAsyncTask implements ISFAsyncTask, Runnable
{
    private ISFAsyncHelper mAsyncHelper;

    @Override
    public void start(ISFAsyncHelper asyncHelper)
    {
        mAsyncHelper = asyncHelper;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run()
    {
        mAsyncHelper.execute();
        mAsyncHelper.onPostExecute();
    }
}