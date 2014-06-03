package com.sharefile.api.exceptions;

public class SFInvalidTypeException extends SFAbstractException 
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