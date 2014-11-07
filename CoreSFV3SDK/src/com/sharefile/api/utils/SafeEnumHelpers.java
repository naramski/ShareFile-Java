package com.sharefile.api.utils;

import com.sharefile.api.models.SFAccessControlFilter;
import com.sharefile.api.models.SFCapabilityName;
import com.sharefile.api.models.SFFileVirusStatus;
import com.sharefile.api.models.SFPreviewStatus;
import com.sharefile.api.models.SFShareType;
import com.sharefile.api.models.SFVRootType;
import com.sharefile.api.models.SFZoneService;
import com.sharefile.api.models.SFZoneType;

public class SafeEnumHelpers 
{
	private static final int BEGIN_INDEX = "com.sharefile.api.enumerations.SFSafeEnum<com.sharefile.api.models.".length();
	
	private static class EnumClassNames
	{		
		public EnumClassNames(String string, Class clazz) 
		{
			mStrName = string;
			mClassName = clazz;
		}
		
		public String mStrName;
		public Class mClassName;
	};
	
	private static final EnumClassNames[] mEnumClassNames = new EnumClassNames[]
	{
		new EnumClassNames("SFFileVirusStatus",SFFileVirusStatus.class),
		new EnumClassNames("SFPreviewStatus",SFPreviewStatus.class),
        new EnumClassNames("SFShareType",SFShareType.class),
        new EnumClassNames("SFCapabilityName",SFCapabilityName.class),
        new EnumClassNames("SFZoneType",SFZoneType.class),
        new EnumClassNames("SFVRootType",SFVRootType.class),
        new EnumClassNames("SFZoneService",SFZoneService.class)
	};
		
	public static Class getEnumClass(String str)
	{
		Class clazz = null;
		String className = str.toString();		
		
		if(className!=null && className.length()>BEGIN_INDEX)
		{
			String containedClassName = className.substring(BEGIN_INDEX, className.length()-1);					
			
			for(EnumClassNames e:mEnumClassNames)
			{
				if(e.mStrName.equals(containedClassName))
				{
					clazz =  e.mClassName;
					break;
				}
			}					
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
	        }
	    }	    
	    return null;
	}
}