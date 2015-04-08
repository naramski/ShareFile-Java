package com.sharefile.api.async;

import com.sharefile.api.interfaces.ISFAsyncTask;
import com.sharefile.api.models.SFODataObject;

public abstract class SFAsyncTaskFactory
{
    private static SFAsyncTaskFactory instance = new SFAsyncTaskFactory()
    {
        @Override
        protected ISFAsyncTask createNewTask()
        {
            return new SFDefaultAsyncTask();
        }
    };

    public static SFAsyncTaskFactory getInstance() { return instance; }

    protected abstract ISFAsyncTask createNewTask();

    public static final void setInstance(SFAsyncTaskFactory newInstance) { instance = newInstance; }

    public static final <T extends SFODataObject> ISFAsyncTask create()
    {
       return getInstance().createNewTask();
    }
}