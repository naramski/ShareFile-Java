package com.sharefile.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

import com.sharefile.api.enumerations.SFSafeEnum;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.models.SFODataObject;

public class SFApiQuery<T> implements ISFQuery<T> 
{

	@Override
	public void setFrom(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHttpMethod(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addIds(URI url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAction(String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addQueryString(String string, Boolean recursive) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBody(SFODataObject sfoDataObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends SFODataObject> void setBody(
			ArrayList<T> sfoDataObjectsFeed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addQueryString(String string, String type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addActionIds(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addQueryString(String string, SFSafeEnum value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addQueryString(String string, Long value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addQueryString(String string, ArrayList<String> ids) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addQueryString(String string, Integer size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addQueryString(String string, Date date) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addSubAction(String string) {
		// TODO Auto-generated method stub
		
	}

}
