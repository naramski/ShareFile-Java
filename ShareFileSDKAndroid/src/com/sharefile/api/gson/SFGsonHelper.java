package com.sharefile.api.gson;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sharefile.api.models.SFODataObject;

public class SFGsonHelper
{	
    private static Gson getGsonBuilder(Type type,Object object)
    {
    	return new GsonBuilder().registerTypeAdapter(type, object).create();    	    	
    }
            
    public static SFODataObject fromJson(String jsonString, Type type, Type typeToken, Object deserializer)
    {
    	Gson gson = SFGsonHelper.getGsonBuilder(type, deserializer);        	        
    	return gson.fromJson(jsonString, typeToken);
    }
    
    public static String getString(JsonObject json,String memberName,String defaultValue)
    {
    	String ret = defaultValue;
    	
    	JsonElement element = json.get(memberName);
    	
    	if(element!=null)
    	{
    		ret = element.getAsString();
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
					ret = new URI(urlspec);
				} 
    			catch (URISyntaxException e) 
    			{					
					e.printStackTrace();
				}
    		}    				
    	}
    	
    	return ret;
    }
        
    public static SFODataObject getSFODataObject(Class clazz,JsonObject json,String memberName,SFODataObject defaultValue)
    {
    	SFODataObject ret = defaultValue;
    	    	    		    	    		    		    	
    	JsonElement element = json.get(memberName);
    	
    	if(element!=null)
    	{    		    		
    		try 
    		{    							
				ret = SFGsonParser.parseFromJson(clazz,element.toString()); 
			} 
    		catch (Exception e) 
    		{				
				e.printStackTrace();
			}     		    		    		    				
    	}    	
    	
    	return ret;
    }
}