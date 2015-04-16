package com.sharefile.api.async;

import com.sharefile.api.interfaces.ISFAsyncTask;
import com.sharefile.api.interfaces.ISFAsyncTaskFactory;

public class SFAsyncTaskFactory implements ISFAsyncTaskFactory
{
    @Override
    public ISFAsyncTask createNewTask()
    {
        return new SFDefaultAsyncTask();
    }
}