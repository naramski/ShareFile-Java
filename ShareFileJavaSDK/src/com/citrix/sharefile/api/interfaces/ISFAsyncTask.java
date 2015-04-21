package com.citrix.sharefile.api.interfaces;

import com.citrix.sharefile.api.async.SFAsyncHelper;

public interface ISFAsyncTask
{
    void start(ISFAsyncHelper asyncHelper);
}
