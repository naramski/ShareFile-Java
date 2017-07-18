package com.citrix.sharefile.api.utils;

import com.citrix.sharefile.api.log.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


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
	/**
	 * Date format pattern used to parse HTTP date headers in RFC 1123 format.
	 */
	private static final  SimpleDateFormat PATTERN_RFC1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

	/**
	 * Date format pattern used to parse HTTP date headers in RFC 1036 format.
	 */
	private static final  SimpleDateFormat PATTERN_RFC1036 = new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss zzz");

	/**
	 * Date format pattern used to parse HTTP date headers in ANSI C
	 */
	private static final  SimpleDateFormat PATTERN_ASCTIME = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");

	private static final TimeZone utcTimezone = TimeZone.getTimeZone("UTC");

	private static final SFDateFormat[] mSFDateFormats = new SFDateFormat[]
	{
		new SFDateFormat(simpleDateFormat),		//Main format for proginey edit/creation dates
		new SFDateFormat(PATTERN_RFC1123),		//Main format for cookie expirations
		new SFDateFormat(simpleDateFormat2),
		new SFDateFormat(PATTERN_RFC1036),		//Obsolete cookie expirations format
		new SFDateFormat(PATTERN_ASCTIME),		//Obsolete cookie expirations format

	};	

	private final SimpleDateFormat mFormat;

	public SFDateFormat(SimpleDateFormat sf)
	{
		mFormat = sf;
		mFormat.setTimeZone(utcTimezone);
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