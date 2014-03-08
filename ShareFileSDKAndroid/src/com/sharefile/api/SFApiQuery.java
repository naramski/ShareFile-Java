package com.sharefile.api;

import java.util.ArrayList;

import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSearchResults;
import com.sharefile.api.models.SFTreeMode;
import com.sharefile.api.models.SFUploadMethod;
import com.sharefile.api.models.SFVRootType;
import com.sharefile.api.models.SFZoneService;

public class SFApiQuery<T> 
{
	public final void setFrom(String setFrom)
	{
		
	}
	
	public final void setAction(String action)
	{
		
	}
	
	public final void setHttpMethod(String httpMethod)
	{
		
	}
	
	public final void addQueryString(String key,String value)
	{
		
	}
	
	public final void addQueryString(String key,Boolean value)
	{
		
	}
	
	public final void addQueryString(String key,SFZoneService sfzoneservice)
	{
		
	}
	
	public final void addIds(String id)
	{
		
	}
	
	public final void addIds(String key,String value)
	{
		
	}
	
	public final void addActionIds(String actionid)
	{
		
	}
	
	public final void addSubAction(String subaction)
	{
		
	}
	
	public final void setBody(SFODataObject body)
	{
		
	}
	
	public <T> void setBody(ArrayList<T> metadata) 
	{
		
	}

	public void addQueryString(String key, SFTreeMode treeMode) 
	{
		
	}

	public void addQueryString(String key, SFVRootType rootType) {
		// TODO Auto-generated method stub
		
	}

	public void addQueryString(String key, ArrayList<String> ids) {
		// TODO Auto-generated method stub
		
	}

	public void addQueryString(String key, Integer size) {
		// TODO Auto-generated method stub
		
	}

	public void addQueryString(String key, SFUploadMethod method) {
		// TODO Auto-generated method stub
		
	}

	public void addQueryString(String key, Long fileSize) {
		// TODO Auto-generated method stub
		
	}

	public void addQueryString(String key, SFApiQuery<SFSearchResults> query) {
		// TODO Auto-generated method stub
		
	}
			
}
