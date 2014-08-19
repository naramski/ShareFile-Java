package com.sharefile.api.exceptions;

@SuppressWarnings("serial")
public class SFOutOfMemoryException extends SFAbstractException 
{
	public SFOutOfMemoryException(String str) 
	{
		super(new Exception(str));		
	}
}
