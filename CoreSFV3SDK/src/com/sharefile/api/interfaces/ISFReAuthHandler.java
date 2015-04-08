package com.sharefile.api.interfaces;

import com.sharefile.api.SFReAuthContext;
import com.sharefile.api.models.SFODataObject;

public interface ISFReAuthHandler 
{
	<T extends SFODataObject> void getCredentials(final SFReAuthContext<T> reauthContext);
	void storeCredentials(final String userName, final String password, final String url, final String userid);
    void wipeCredentials(final String url, final String userid);
}
