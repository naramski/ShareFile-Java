package com.sharefile.api.interfaces;

import com.sharefile.api.async.SFAsyncHelper;

public interface ISFAsyncTask
{
    void start(ISFAsyncHelper asyncHelper);
}
