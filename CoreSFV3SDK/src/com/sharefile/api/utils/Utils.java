package com.sharefile.api.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;

import com.sharefile.api.SFV3Error;
import com.sharefile.api.interfaces.ISFApiResultCallback;
import com.sharefile.api.interfaces.ISFQuery;

public class Utils 
{
	public static String getAcceptLanguageString()
	{
		Locale currentLocale = Locale.getDefault();
	    return currentLocale.toString().replace('_', '-') + ";q=0.8,en;q=0.6";
	}
		
	public static <T> void safeCallErrorListener(ISFApiResultCallback<T> mListener, SFV3Error error, ISFQuery<T> sfapiApiqueri)
	{
		if(mListener!=null)
		{
			mListener.onError(error, sfapiApiqueri);
		}	
	}
		
	public static <T> void safeCallSuccess(ISFApiResultCallback<T> listener, T object)
	{
		if(listener!=null)
		{
			listener.onSuccess(object);
		}	
	}
	
	public static String parseV3IDFromURL(String url)
	{
		   String ret = "";
		   
		   if(url!=null && url.length()>2)
		   {
			   int startIndex = url.indexOf("/Items(");
			   int endIndex = url.indexOf(")");
			   if(startIndex>0)
			   {
				   ret = url.substring(startIndex + 7,endIndex);
			   }
		   }
		   
		   return ret;
	}

    public static String parseV3IDFromURL(URI url)
    {
        if(url == null)
        {
             return null;
        }

        return parseV3IDFromURL(url.toString());
    }

	public static boolean isEmpty(String str)
	{
		boolean ret = false;
		
		if(str == null || str.length() ==0 )
		{
			ret = true;
		}
		
		return ret;
	}

    public static boolean isEmpty(ArrayList arrayList)
    {
        boolean ret = false;

        if(arrayList == null || arrayList.size() ==0 )
        {
            ret = true;
        }

        return ret;
    }

    public static boolean isConnectorGroup(String id)
    {
        return id.indexOf("c-")==0;
    }
}