package com.citrix.sharefile.api.entities;

import com.citrix.sharefile.api.interfaces.ISFApiClient;

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