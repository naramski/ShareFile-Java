package com.citrix.sharefile.api;

import com.citrix.sharefile.api.constants.SFKeywords;
import com.citrix.sharefile.api.constants.SFSdkGlobals;
import com.citrix.sharefile.api.log.Logger;
import com.citrix.sharefile.api.models.SFLockType;
import com.citrix.sharefile.api.utils.Utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class SFProvider
{
    private static final String TAG = SFKeywords.TAG + "-getProvider";
    public static final  String PROVIDER_TYPE_SF = "sf";

    /**
	 * 	String can be of type : 
	 *  <p>https://szqatest2.sharefiletest.com/cifs/v3/Items(4L24TVJSEz6Ca22LWoZg41hIVgfFgqQx0GD2VoYSgXA_)</p>
	 *  
	 *  or
	 *  <p>
	 *  "https://szqatest2.sharefiletest.com/sp/upload-streaming-2.aspx?uploadid=rsu-27564a05c0cf4052989099f3e880afda&parentid
	 *  
	 *  <p>This function finds the provider based on the occurence of /sf/v3/ , /sp/v3/ , /cifs/v3/ whichever occurs first
	 *
	 */
    public static String getProviderType(String urlString)
    {
        if(Utils.isEmpty(urlString))
        {
            return "";
        }

        try
        {
            return getProviderType(new URI(urlString));
        }
        catch (URISyntaxException e)
        {
            Logger.e(TAG,e);
        }

        return "";
    }
	
	public static String getProviderType(URI uri)
	{
        String path = uri.getPath();

        if(path == null || path.length() < 2 ) //we check this since the path should always contain /v3/ or /upload-streaming
        {
            return "";
        }

        int indexOfSecondSlash = path.indexOf('/',1);

        if(indexOfSecondSlash < 0)
        {
            return "";
        }

        return path.substring(1,indexOfSecondSlash);
	}

    public static String getProviderType(URL url)
    {
        if(url==null)
        {
            return "";
        }

        try
        {
            return getProviderType(url.toURI());
        }
        catch (URISyntaxException e)
        {
            Logger.e(TAG,e);
        }

        return "";
    }
}