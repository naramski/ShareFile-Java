package com.sharefile.api.interfaces;

public interface SFApiClientInitListener 
{
	public void sfApiClientInitSuccess();
	public void sfApiClientInitError(int errorCode,String errorMessage);
}