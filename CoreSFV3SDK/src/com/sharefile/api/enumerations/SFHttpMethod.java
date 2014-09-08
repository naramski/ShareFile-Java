package com.sharefile.api.enumerations;

public enum SFHttpMethod 
{
	GET("GET"),
	POST("POST"),
	PUT("PUT"),
	PATCH("PATCH"),
	DELETE("DELETE"),
	UPDATE("UPDATE");
		
	private final String mToString;
	
	private SFHttpMethod(String toStr) 
	{		
		mToString = toStr;
	}
	
	@Override
	public String toString() 
	{		
		return mToString;
	}
}