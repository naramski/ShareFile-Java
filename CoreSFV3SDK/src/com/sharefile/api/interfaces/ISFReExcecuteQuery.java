package com.sharefile.api.interfaces;

import com.sharefile.api.SFApiClient;
import com.sharefile.api.models.SFODataObject;

@Deprecated
public interface ISFReExcecuteQuery<T>
{
	public void execute(SFApiClient sfApiClient , ISFQuery<T> query , SFApiResponseListener<T> listener, ISFReAuthHandler reauthHandler);
}