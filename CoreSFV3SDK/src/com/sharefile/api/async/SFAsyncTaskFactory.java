package com.sharefile.api.async;

import com.sharefile.api.interfaces.ISFApiClient;
import com.sharefile.api.interfaces.ISFApiResultCallback;
import com.sharefile.api.interfaces.ISFAsyncTask;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.models.SFODataObject;

public abstract class SFAsyncTaskFactory
{
    private static SFAsyncTaskFactory instance = new SFAsyncTaskFactory()
    {
        @Override
        protected <T extends SFODataObject> ISFAsyncTask
        createNewTask(ISFApiClient apiClient, ISFQuery<T> query, ISFApiResultCallback<T> callback)
        {
            return new SFAsyncTask(apiClient,query,callback);
        }
    };

    public static SFAsyncTaskFactory getInstance() { return instance; }

    protected abstract <T extends SFODataObject> ISFAsyncTask createNewTask(ISFApiClient apiClient,
                                         ISFQuery<T> query,
                                         ISFApiResultCallback<T> callback);

    public static final void setInstance(SFAsyncTaskFactory newInstance) { instance = newInstance; }

    public static final <T extends SFODataObject> ISFAsyncTask create(ISFApiClient apiClient,
                                                                ISFQuery<T> query,
                                                                ISFApiResultCallback<T> callback)
    {
       return getInstance().createNewTask(apiClient,query,callback);
    }
}