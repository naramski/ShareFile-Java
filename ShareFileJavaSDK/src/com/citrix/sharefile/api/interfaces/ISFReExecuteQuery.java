package com.citrix.sharefile.api.interfaces;

import com.citrix.sharefile.api.SFApiClient;

public interface ISFReExecuteQuery<T>
{
	public void execute(ISFApiClient sfApiClient , ISFQuery<T> query , ISFApiResultCallback<T> listener, ISFReAuthHandler reauthHandler);
}