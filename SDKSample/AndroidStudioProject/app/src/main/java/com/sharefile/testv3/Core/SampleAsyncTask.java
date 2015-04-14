package com.sharefile.testv3.Core;

import android.os.AsyncTask;

import com.sharefile.api.interfaces.ISFAsyncHelper;
import com.sharefile.api.interfaces.ISFAsyncTask;

public class SampleAsyncTask extends AsyncTask implements ISFAsyncTask
{
    ISFAsyncHelper asyncHelper;

    @Override
    protected Object doInBackground(Object[] objects)
    {
        asyncHelper.execute();
        return null;
    }

    @Override
    protected void onPostExecute(Object o)
    {
        super.onPostExecute(o);
        asyncHelper.onPostExecute();
    }

    @Override
    public void start(ISFAsyncHelper asyncHelper)
    {
        this.asyncHelper = asyncHelper;
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}