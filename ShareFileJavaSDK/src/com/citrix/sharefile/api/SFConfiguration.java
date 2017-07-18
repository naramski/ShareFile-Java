package com.citrix.sharefile.api;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.citrix.sharefile.api.constants.SFKeywords;

/**
 *   Allows applications to specify specific configuaration settings and https headers to be added
 *   before the SDK makes urlconnection to the server.
 */
public class SFConfiguration 
{
	public static final boolean RESOLVE_LOCALE = true;
	// SF-Locale configuration support.
	// ?More info: http://www.oracle.com/us/technologies/java/locale-140624.html?
	public static final boolean LOCALE_COUNTRY_SUPPORT = true;
	public static final boolean LOCALE_VARIANT_SUPPORT = false;
	public static final boolean LOCALE_SCRIPT_SUPPORT = false;

	private final Map<String ,String> mAdditionalHttpHeaders = new HashMap<String,String>();
		
	public void addAcceptedLanguages(ArrayList<String> acceptedLanguages) 
	{
		if(acceptedLanguages == null || acceptedLanguages.size() == 0)
		{
			return;
		}
		
		StringBuilder sb = new StringBuilder();
				
		for(String str:acceptedLanguages)
		{
			sb.append(str);
			sb.append(";");
		}
		
		appendToPrevious(SFKeywords.ACCEPT_LANGUAGE, sb.toString());
	}
	
	public void addAcceptedLanguage(String acceptedLanguage) 
	{		
		appendToPrevious(SFKeywords.ACCEPT_LANGUAGE, acceptedLanguage);
	}
	
	/**
	 *  This is generic provision to add any header.
	 */
	public void addHeader(String name,String value)
	{
		mAdditionalHttpHeaders.put(name, value);
	}
	
	public void removeHeader(String name)
	{
		mAdditionalHttpHeaders.remove(name);
	}
	
	public void removeAllHeaders()
	{
		mAdditionalHttpHeaders.clear();
	}
	
	private void appendToPrevious(String name,String newvalue)
	{
		String previousValue = mAdditionalHttpHeaders.get(name);
		
		if(previousValue == null)
		{
			previousValue = "";
		}
				
		mAdditionalHttpHeaders.put(name, previousValue + newvalue);
	}
	
	public void setAddtionalHeaders(URLConnection conn)
	{
		if(conn == null)
		{
			return;
		}

        for (Entry<String, String> pair : mAdditionalHttpHeaders.entrySet()) {
            conn.setRequestProperty(pair.getKey(), pair.getValue());
        }
	}
	
}