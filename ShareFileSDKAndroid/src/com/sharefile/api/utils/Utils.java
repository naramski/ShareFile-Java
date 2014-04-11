package com.sharefile.api.utils;

import java.util.Locale;

public class Utils 
{
	public static String getAcceptLanguageString()
	{
		Locale currentLocale = Locale.getDefault();
	    String acceptLanguageString = currentLocale.toString().replace('_', '-') + ";q=0.8,en;q=0.6";
	    return acceptLanguageString;
	}
}
