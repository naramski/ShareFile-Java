package com.sharefile.api.async;

import com.sharefile.api.SFSDKDefaultAccessScope;
import com.sharefile.api.interfaces.ISFApiClient;
import com.sharefile.api.interfaces.ISFApiResultCallback;
import com.sharefile.api.interfaces.ISFAsyncTask;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.models.SFODataObject;

@SFSDKDefaultAccessScope
class SFDefaultAsyncTask implements ISFAsyncTask, Runnable
{
    private SFAsyncHelper mAsyncHelper;

    public <T extends SFODataObject> SFDefaultAsyncTask()
    {

    }

    @Override
    public void start(SFAsyncHelper asyncHelper)
    {
        mAsyncHelper = asyncHelper;
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