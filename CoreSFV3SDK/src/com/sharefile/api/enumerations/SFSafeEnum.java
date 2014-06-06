package com.sharefile.api.enumerations;

import com.google.gson.annotations.SerializedName;

public class SFSafeEnum<T> 
{
	@SerializedName("value")
	private String value;
	
	public void setValue(String v)
	{
		value = v;
	}
	
	public String getValue()
	{
		return value;
	}	
}
