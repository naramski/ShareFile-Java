package com.sharefile.api.interfaces;

import com.sharefile.api.SFApiClient;

public interface ISFReExecuteQuery<T>
{
	public void execute(SFApiClient sfApiClient , ISFQuery<T> query , ISFApiResultCallback<T> listener, ISFReAuthHandler reauthHandler);
}