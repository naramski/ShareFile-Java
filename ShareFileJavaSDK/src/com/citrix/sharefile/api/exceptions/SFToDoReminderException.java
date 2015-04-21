package com.citrix.sharefile.api.exceptions;

public class SFToDoReminderException extends RuntimeException 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4039761602528133691L;
	
	private static final String NEED_TO_IMPLEMENT = "Need to implement";
	
	public SFToDoReminderException(String str) 
	{		
		super(str);
	}

	/**
	 *  Simple TODO statements in the code get forgotten. throw an exception that will remind us of the important parts of 
	 *  code that actually gets executed and needs implementing. We can remove these calls later. 	   
	 */
	public static void throwTODOException(String str)
	{
		throw new SFToDoReminderException(NEED_TO_IMPLEMENT +"-->"+ str);
	}
}
