package com.citrix.sharefile.api.gson;

import com.citrix.sharefile.api.constants.SFKeywords;
import com.citrix.sharefile.api.log.Logger;
import com.citrix.sharefile.api.utils.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *   This class contains helper get*() functions to get primitives out of gson objects 
 *   whenever we need manual parsing sometimes.
 */
public class SFGsonHelper
{	
	private static final String TAG = SFKeywords.TAG + "-SFGsonHelper";
	        
    public static String getString(JsonObject json,String memberName,String defaultValue)
    {
    	String ret = defaultValue;
    	
    	JsonElement element = json.get(memberName);
    	
    	
    	if(element!=null)
    	{
    		try
    		{
    			ret = element.getAsString();
    		}
    		catch(Exception e)
    		{
    			ret = element.toString();
    		}
    	}
    	
    	return ret;
    }
    
    public static int getInt(JsonObject json,String memberName,int defaultValue)
    {
    	int ret = defaultValue;
    	
    	JsonElement element = json.get(memberName);
    	
    	if(element!=null)
    	{
    		ret = element.getAsInt();
    	}
    	
    	return ret;
    }
    
    public static long getLong(JsonObject json,String memberName,long defaultValue)
    {
    	long ret = defaultValue;
    	
    	JsonElement element = json.get(memberName);
    	
    	if(element!=null)
    	{
    		ret = element.getAsLong();
    	}
    	
    	return ret;
    }
    
    public static boolean getBoolean(JsonObject json,String memberName,boolean defaultValue)
    {
    	boolean ret = defaultValue;
    	
    	JsonElement element = json.get(memberName);
    	
    	if(element!=null)
    	{
    		ret = element.getAsBoolean();
    	}
    	
    	return ret;
    }

    public static URI getURI(JsonObject json,String memberName,URI defaultValue)
    {
    	URI ret = defaultValue;
    	
    	JsonElement element = json.get(memberName);
    	
    	if(element!=null)
    	{
    		String urlspec = element.getAsString();
    		
    		if(urlspec!=null)
    		{
    			try 
    			{
					return Utils.getURIFromString(urlspec);
				} 
    			catch (URISyntaxException | MalformedURLException | UnsupportedEncodingException e)
    			{					
					Logger.e(TAG,e);
				}
    		}    				
    	}
    	
    	return ret;
    }
}