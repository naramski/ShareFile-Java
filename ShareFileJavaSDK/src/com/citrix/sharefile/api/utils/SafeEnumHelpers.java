package com.citrix.sharefile.api.utils;

import com.citrix.sharefile.api.log.Logger;

/**
	 The way this parsing works relies on the class name sent by the server.
	 So if you use proguard in your application, make sure to add the following lines to the
	 proguard config file:

	 -keepattributes Signature
	 -keepattributes *Annotation*
	 -keep class com.citrix.sharefile.api.entities.** { *; }
	 -keep class com.citrix.sharefile.api.models.** { *; }

      Sample name sent by the server :

      "com.citrix.sharefile.api.enumerations.SFSafeEnum<com.citrix.sharefile.api.models.SFCapabilityName>"
 */
public class SafeEnumHelpers 
{
	private static final String TAG = "SafeEnumHelpers";

	public static Class getEnumClass(String className)
	{
		if(className == null || className.length() == 0)
		{
			return null;
		}

		String containedClassName = className;
		Class clazz = null;

		try
		{
			int beginIndex = className.indexOf("<");
			if(beginIndex > -1 )
			{
				containedClassName = className.substring(beginIndex+1, className.length() - 1);
			}

			clazz = Class.forName(containedClassName);
		}
		catch (ClassNotFoundException e)
		{
			Logger.e(TAG,e);
		}
		catch (Exception e)
		{
			Logger.e(TAG,e);
		}

		return clazz;
	}
	
	public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string)
	{
	    if( c != null && string != null )
	    {
	        try
	        {
	            return Enum.valueOf(c, string);
	        }
	        catch(IllegalArgumentException ex)
	        {
                Logger.e("SafeEnumHelper",ex);
	        }
	    }	    
	    return null;
	}
}