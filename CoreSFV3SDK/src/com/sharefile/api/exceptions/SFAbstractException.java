package com.sharefile.api.exceptions;

public abstract class SFAbstractException extends Exception 
{
	/**
	 * [auto-generated]
	 */
	private static final long serialVersionUID = 6649156220182154390L;
	
	private final Exception mOrigException;
	
	public SFAbstractException(Exception ex)
	{
		if(ex!=null)
		{
			mOrigException = ex;
		}
		else			
		{			
			throw new NullPointerException("SFException should not be null.");
		}
	}
	
	@Override
	public Throwable getCause() 
	{		
		return mOrigException.getCause();
	}
	
	@Override
	public StackTraceElement[] getStackTrace() 
	{		
		return mOrigException.getStackTrace();
	}
		
	@Override
	public String getLocalizedMessage() 
	{				
		return mOrigException.getLocalizedMessage();
	}
	
	@Override
	public String getMessage() 
	{		
		return mOrigException.getMessage();
	}
}
