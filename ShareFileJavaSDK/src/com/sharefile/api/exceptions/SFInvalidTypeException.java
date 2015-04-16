package com.sharefile.api.exceptions;

public class SFInvalidTypeException extends SFSDKException
{
	public SFInvalidTypeException(Exception ex) 
	{
		super(ex);		
	}
	
	public SFInvalidTypeException(String msg) 
	{
		super(new RuntimeException(msg));		
	}
}