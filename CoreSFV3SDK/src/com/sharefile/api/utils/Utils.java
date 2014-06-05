package com.sharefile.api.utils;

import java.util.Locale;

import com.sharefile.api.SFV3Error;
import com.sharefile.api.interfaces.ISFQuery;
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
		
	public static <T extends SFODataObject> void safeCallErrorListener(SFApiResponseListener<T> mListener, SFV3Error error, ISFQuery<T> sfapiApiqueri)
	{
		if(mListener!=null)
		{
			mListener.sfApiError(error, sfapiApiqueri);
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
