package com.sharefile.api;

import java.util.ArrayList;

import com.sharefile.api.enumerations.SFTreeMode;
import com.sharefile.api.enumerations.SFUploadMethod;
import com.sharefile.api.enumerations.SFVRootType;
import com.sharefile.api.enumerations.SFZoneService;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSearchResults;

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
		
		
	}

	public void addQueryString(String key, ArrayList<String> ids) {
		
		
	}

	public void addQueryString(String key, Integer size) {
		
		
	}

	public void addQueryString(String key, SFUploadMethod method) {
		
		
	}

	public void addQueryString(String key, Long fileSize) {
		
		
	}

	public void addQueryString(String key, SFApiQuery<SFSearchResults> query) {
		
		
	}
			
}
