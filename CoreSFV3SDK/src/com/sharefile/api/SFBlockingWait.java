package com.sharefile.api;

import com.sharefile.api.exceptions.SFToDoReminderException;

public final class SFBlockingWait 
{
	private final Object mWaitObject = new Object();
	private static final long WAIT_TIMEOUT = 60*1000; // Currently keep a 60 seconds time-out. Later make this configurable if needed.
	
	/**
	 *  Wait until a timeout. None of the ShareFile operations can wait indefinitely.
	 */
	public void blockingWait()
	{		
		synchronized (mWaitObject) 
		{
			try 
			{
				mWaitObject.wait(WAIT_TIMEOUT);
			} 
			catch (InterruptedException e) 
			{				
				SFToDoReminderException.throwTODOException("Blocking wait interrupted.");
			}
		}
	}
	
	/**
	 * Use this with caution. Never use this inside one of the SDK calls.
	 */
	public void blockingWaitInfinite()
	{		
		synchronized (mWaitObject) 
		{
			try 
			{
				mWaitObject.wait();
			} 
			catch (InterruptedException e) 
			{				
				SFToDoReminderException.throwTODOException("Blocking wait interrupted.");
			}
		}
	}
	
	public void unblockWait()
	{
		synchronized (mWaitObject) 
		{			
			mWaitObject.notifyAll();
		}
	}
}
