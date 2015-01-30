package com.sharefile.api.interfaces;

import com.sharefile.api.SFReAuthContext;
import com.sharefile.api.models.SFODataObject;

public interface ISFReAuthHandler 
{
	public <T extends SFODataObject> void getCredentials(final SFReAuthContext<T> reauthContext);
	public void storeCredentials(final String userName, final String password, final String url, final String userid);
    public void wipeCredentials(final String url, final String userid);
}
