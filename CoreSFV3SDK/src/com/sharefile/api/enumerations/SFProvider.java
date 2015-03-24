package com.sharefile.api.enumerations;

import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.log.Logger;

import java.net.URI;

/**
 *  toString of this will return complete provider with API version alongwith slashes: "/cifs/v3/", "/sp/v3/", "/sf/v3/",... etc
 */
public enum SFProvider 
{		
	PROVIDER_TYPE_SF("/sf/"+SFSDK.VERSION_FOR_QUERY_URL+ SFKeywords.FWD_SLASH),
	PROVIDER_TYPE_CIFS("/cifs/"+SFSDK.VERSION_FOR_QUERY_URL+ SFKeywords.FWD_SLASH),
	PROVIDER_TYPE_SHAREPOINT("/sp/"+SFSDK.VERSION_FOR_QUERY_URL+ SFKeywords.FWD_SLASH),
	PROVIDER_TYPE_PROXYSERVICE("/ProxyService/"+SFSDK.VERSION_FOR_QUERY_URL+ SFKeywords.FWD_SLASH);
	
	private static final String keywordV3 = SFKeywords.FWD_SLASH+SFSDK.VERSION_FOR_QUERY_URL+ SFKeywords.FWD_SLASH;
	private static final String TAG = SFKeywords.TAG + "-getProvider";
	
	private final String mToStr;
	
	private SFProvider(String toStr)
	{
		mToStr = toStr;
	}
	
	@Override
	public String toString() 
	{		
		return mToStr;
	}
	
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
	 *  We check all of them since multiple of them may occur in a given string, but the first occurence defines the provider type
	 */
	public static SFProvider getProviderType(String str)
	{
		SFProvider provider = PROVIDER_TYPE_SF; //return sf by default so as not to cause NullPointer exceptions
												//for bad strings.
				
		if(str!=null)
		{
			/*
			 * look for the first occurence of "/v3/" and then look to the left of it. 
			 * this way we don't waste too much time in string comparison.
			 */			 													
			int indexOfV3 = str.indexOf(keywordV3);

			if(indexOfV3 == -1)
			{
                // is needed or the upload to connectors wont work.
				indexOfV3 = str.indexOf("/upload-streaming");
			}

            if(indexOfV3 <1)
            {
                return provider;
            }

            try
			{
				//HACK optimize: look for the first chracter before this index since. can change this later if things break. 			
				switch(str.charAt(indexOfV3-1))
				{
					case 'p': provider = PROVIDER_TYPE_SHAREPOINT;
			    		break;
					case 'f': provider = PROVIDER_TYPE_SF;
				    	break;
					case 's': provider = PROVIDER_TYPE_CIFS;
					    break;
					case 'e': provider = PROVIDER_TYPE_PROXYSERVICE;
					    break;
				}
			}
			catch(Exception ex)
			{
				Logger.d(TAG, "!!!Exception getting provider type from: " + str, ex);
			}
		}
				
		Logger.d(TAG, "Returning provider type = %s" + provider.toString());
		
		return provider;
	}
	
	public static SFProvider getProviderType(URI uri)
	{
		String str = null;
		
		if(uri!=null)
		{
			str = uri.toString();
		}
		
		return getProviderType(str);
	}
}