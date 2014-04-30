package com.sharefile.api.utils;

import java.util.Locale;

import com.sharefile.api.SFApiQuery;
import com.sharefile.api.V3Error;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFODataObject;

public class Utils 
{
	public static String getAcceptLanguageString()
	{
		Locale currentLocale = Locale.getDefault();
	    String acceptLanguageString = currentLocale.toString().replace('_', '-') + ";q=0.8,en;q=0.6";
	    return acceptLanguageString;
	}
		
	public static <T extends SFODataObject> void safeCallErrorListener(SFApiResponseListener<T> listener, V3Error error, SFApiQuery<T> query)
	{
		if(listener!=null)
		{
			listener.sfApiError(error, query);
		}	
	}
		
	public static <T extends SFODataObject> void safeCallSuccess(SFApiResponseListener<T> listener, T object)
	{
		if(listener!=null)
		{
			listener.sfApiSuccess(object);
		}	
	}
}
