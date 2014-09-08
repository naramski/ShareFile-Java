package com.sharefile.api.enumerations;

public enum SFHttpMethod 
{
	GET("GET"),
	POST("POST"),
	PUT("PUT"),
	PATCH("PATCH"), //Android was throwing Protocol exception on this Verb during last test since PATCH is not recognized.
	DELETE("DELETE");
		
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