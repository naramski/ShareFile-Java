package com.sharefile.api.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;

import com.sharefile.api.constants.SFSdkGlobals;
import com.sharefile.api.SFProvider;
import com.sharefile.api.exceptions.SFSDKException;
import com.sharefile.api.interfaces.ISFApiResultCallback;
import com.sharefile.api.interfaces.ISFQuery;

public class Utils 
{
    private static final String FORMAT_GET_TOP_FOLDER = "https://%s.%s/"+ SFProvider.PROVIDER_TYPE_SF+"/v3/Items(%s)";
    private static final String FORMAT_GET_DEVICES = "https://%s.%s/"+SFProvider.PROVIDER_TYPE_SF+"/v3/Devices(%s)";

	public static String getAcceptLanguageString()
	{
		Locale currentLocale = Locale.getDefault();
	    return currentLocale.toString().replace('_', '-') + ";q=0.8,en;q=0.6";
	}
		
	public static <T> void safeCallErrorListener(ISFApiResultCallback<T> mListener, SFSDKException error, ISFQuery<T> sfapiApiqueri)
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

    /**
     *   We need to manually construct the v3 url for the TOP folder. This function provides the helper for the apps
     *   to build that url.
     */
    public static final URI getDefaultURL(final String subdomain,String hostname,final String folderID) throws URISyntaxException
    {
        URI uri;

        String urlSpec = String.format(FORMAT_GET_TOP_FOLDER, subdomain, SFSdkGlobals.getApiServer(hostname),folderID);

        uri = new URI(urlSpec);

        return uri;
    }

    public static final URI getDeviceURL(final String subdomain, String hostname, final String deviceID) throws URISyntaxException
    {
        URI uri;

        String urlSpec = String.format(FORMAT_GET_DEVICES, subdomain, SFSdkGlobals.getApiServer(hostname),deviceID);

        uri = new URI(urlSpec);

        return uri;
    }
}