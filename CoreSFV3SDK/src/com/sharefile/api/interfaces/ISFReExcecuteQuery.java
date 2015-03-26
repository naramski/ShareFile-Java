package com.sharefile.api.interfaces;

import com.sharefile.api.SFApiClient;

@Deprecated
public interface ISFReExcecuteQuery<T>
{
	public void execute(SFApiClient sfApiClient , ISFQuery<T> query , ISFApiCallback<T> listener, ISFReAuthHandler reauthHandler);
}