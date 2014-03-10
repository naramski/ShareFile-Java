package com.sharefile.api.exceptions;

@SuppressWarnings("serial")
public class SFInvalidStateException extends SFAbstractException 
{
	public SFInvalidStateException(String msg) 
	{
		super(new RuntimeException(msg));		
	}
	
	public SFInvalidStateException(Exception ex) 
	{
		super(ex);		
	}
}
