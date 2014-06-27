package com.sharefile.api.utils;

import com.sharefile.java.log.SLog;

/**
 *  The Logcat log gets truncated. We want to be able to see the entire server response during debugging
 *  so create this simple helper. We may not need this for prodcution.
 */
public class SFDumpLog 
{
	private static final int mLogLimit = 2000;
	
	private static void longLog(String str) 
	{
	    if(str!=null && str.length() > mLogLimit) 
	    {
	        SLog.d(str.substring(0, mLogLimit));
	        longLog(str.substring(mLogLimit));
	    } 
	    else
	    {
	        SLog.d(str);
	    }
	}
	
	public static void dumpLog(String TAG, String header,String str)
	{
		SLog.d(TAG, header+" START-------------");
		
		longLog(str); 
		
		SLog.d(TAG, header+" -------------END");
	}
}
