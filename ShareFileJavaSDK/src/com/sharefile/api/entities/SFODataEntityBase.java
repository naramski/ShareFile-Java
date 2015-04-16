package com.sharefile.api.entities;

import com.sharefile.api.interfaces.ISFApiClient;

public class SFODataEntityBase
{
    protected final ISFApiClient apiClient;

    public SFODataEntityBase(ISFApiClient client)
    {
        this.apiClient = client;
    }

    public SFODataEntityBase()
    {
        this.apiClient = null;
    }
}