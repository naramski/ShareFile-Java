package com.citrix.sharefile.api.utils;

import com.citrix.sharefile.api.log.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SFDateFormat
{
	private static final String TAG = "SFDateFormat";
	
	
	//Add more ShareFile date formats if newer formats get added
	//Don't do zone replace. Java versions less than 8 have severe bugs and difference between Android/Desktop
	//JodaTime is no fun either.
	//This date format needs Zone replacement which causes erroneous dates on Java.
	//private static final  SimpleDateFormat v3SimpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSSZ");
	private static final  SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss");
    private static final  SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("EEEE, dd MMM yyyy HH:mm:ss z");
	
	private static final SFDateFormat[] mSFDateFormats = new SFDateFormat[]
	{
		new SFDateFormat(simpleDateFormat),
        new SFDateFormat(simpleDateFormat2)
	};	

	private final SimpleDateFormat mFormat;

	public SFDateFormat(SimpleDateFormat sf)
	{
		mFormat = sf;
	}
	
	private static Date parse(SFDateFormat format, String str)
	{
		Date ret = null;
		
		try 
		{			
			ret = format.mFormat.parse(str);
		} 
		catch (ParseException e) 
		{				
			//don't log this to reduce the noise
		}
		catch (Exception e) 
		{
			Logger.e(TAG,e);
		}
		
		return ret;
	}
			
	public static Date parse(String str)
	{
		Date ret = null;
		
		if(str == null)
		{
			return null;
		}
		
		for(SFDateFormat d:mSFDateFormats)
		{
			ret = SFDateFormat.parse(d,str);
			
			if(ret != null)
			{
				break;
			}
		}
		
		if(ret == null)
		{
			Logger.e(TAG, "cannot parse date: " + str);
		}
		
		return ret;
	}	
	
	public static String getUserFriendlyDateString(Date date)
	{
		if(date == null)
		{
			return "";
		}
		
		DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        return df.format(date);
	}
}