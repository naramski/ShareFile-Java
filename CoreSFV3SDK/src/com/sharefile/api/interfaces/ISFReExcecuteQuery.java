package com.sharefile.api.interfaces;

import com.sharefile.api.SFApiClient;
import com.sharefile.api.models.SFODataObject;

public interface ISFReExcecuteQuery<T extends SFODataObject> 
{
	public void execute(SFApiClient sfApiClient , ISFQuery<T> query , SFApiResponseListener<T> listener, ISFReAuthHandler reauthHandler);
}