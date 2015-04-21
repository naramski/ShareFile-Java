package com.citrix.sharefile.api.interfaces;

import com.citrix.sharefile.api.SFCredential;

import java.net.URL;

public interface ISFReAuthHandler 
{
	public SFCredential getCredentials(String url, ISFApiClient apiClient);
	public void storeCredentials(SFCredential credential, String url, ISFApiClient apiClient);
    public void wipeCredentials(String url, ISFApiClient apiClient);
}