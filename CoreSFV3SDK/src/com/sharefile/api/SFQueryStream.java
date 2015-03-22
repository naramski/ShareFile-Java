package com.sharefile.api;

import com.sharefile.api.interfaces.ISFApiClient;

public class SFQueryStream<T> extends SFApiQuery
{
    public SFQueryStream(ISFApiClient client)
    {
        super(client);
    }
}