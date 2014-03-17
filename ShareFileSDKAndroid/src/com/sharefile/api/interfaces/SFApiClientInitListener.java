package com.sharefile.api.interfaces;

import com.sharefile.api.V3Error;

public interface SFApiClientInitListener 
{
	public void sfApiClientInitSuccess();
	public void sfApiClientInitError(V3Error error);
}