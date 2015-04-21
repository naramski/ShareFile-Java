package com.citrix.sharefile.api.async;

import com.citrix.sharefile.api.interfaces.ISFAsyncTask;
import com.citrix.sharefile.api.interfaces.ISFAsyncTaskFactory;

public class SFAsyncTaskFactory implements ISFAsyncTaskFactory
{
    @Override
    public ISFAsyncTask createNewTask()
    {
        return new SFDefaultAsyncTask();
    }
}