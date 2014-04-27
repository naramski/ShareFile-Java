package com.sharefile.api.interfaces;

import com.sharefile.api.SFReAuthContext;

public interface ISFReAuthHandler 
{
	public void getCredentials(final SFReAuthContext reauthContext);
}
