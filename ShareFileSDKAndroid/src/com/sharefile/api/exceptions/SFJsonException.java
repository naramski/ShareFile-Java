package com.sharefile.api.exceptions;

@SuppressWarnings("serial")
public class SFJsonException extends SFAbstractException
{
	public SFJsonException(Exception ex) 
	{
		super(ex);		
	}				
}