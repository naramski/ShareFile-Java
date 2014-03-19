package com.sharefile.api.enumerations;

import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;

/**
 *  toString of this will return complete provider with API version alongwith slashes: "/cifs/v3/", "/sp/v3/", "/sf/v3/",... etc
 */
public enum SFProvider 
{	
	PROVIDER_TYPE_SF{@Override public String toString() {return "/sf/"+SFSDK.VERSION_FOR_QUERY_URL+ SFKeywords.FWD_SLASH;}},
	PROVIDER_TYPE_CIFS{@Override public String toString() {return "/cifs/"+SFSDK.VERSION_FOR_QUERY_URL+ SFKeywords.FWD_SLASH;}},
	PROVIDER_TYPE_SHAREPOINT{@Override public String toString() {return "/sp/"+SFSDK.VERSION_FOR_QUERY_URL+ SFKeywords.FWD_SLASH;}};
	
	private static final String keywordV3 = SFKeywords.FWD_SLASH+SFSDK.VERSION_FOR_QUERY_URL+ SFKeywords.FWD_SLASH;
	
	/**
	 * 	String can be of type : 
	 *  <p>https://szqatest2.sharefiletest.com/cifs/v3/Items(4L24TVJSEz6Ca22LWoZg41hIVgfFgqQx0GD2VoYSgXA_)</p>
	 *  
	 *  This function finds the provider based on the occurence of /sf/v3/ , /sp/v3/ , /cifs/v3/ whichever occurs first
	 *  
	 *  We check all of them since multiple of them may occur in a given string, but the first occurence defines the provider type
	 */
	public static SFProvider getProviderTypeFromString(String str)
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
				}
			}
			catch(Exception ex)
			{
				SFLog.d2("-getProvider", "!!!Exception getting provider type from: %s", str);
			}
		}
				
		SFLog.d2("-getProvider", "Returning provider type = %s", provider.toString());
		
		return provider;
	}
}