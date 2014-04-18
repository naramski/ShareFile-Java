package com.sharefile.api;

/**
 *   This is used to return the objects from callbacks. Several of our callbacks are anonymous objects 
 *   inside other functions and return success or error becomes complex unless we have final object inside the function.
 *   We use this class to surrogate the return values.
 */
public class SFReturnWrapper<T> 
{
	private T mRetObject = null;
	
	public synchronized T getReturnObject()
	{
		return mRetObject;
	}
	
	public void storeObject(T retObject)
	{
		mRetObject = retObject;
	}		
}
