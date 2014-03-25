package com.sharefile.api.gson;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.enumerations.SFV3FeedType;
import com.sharefile.api.gson.auto.SFDefaultGsonParser;
import com.sharefile.api.models.SFFile;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFODataFeed;
import com.sharefile.api.models.SFODataObject;

/**
 *   This class contains helper get*() functions to get primitives out of gson objects 
 *   whenever we need manual parsing sometimes.
 */
public class SFGsonHelper
{	
	private static final String TAG = "-SFGsonHelper";
	        
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
    
    public static <T> ArrayList<T> getArrayList(Class clazz, JsonObject json,String memberName,ArrayList<T> defaultValue)
    {
    	ArrayList<T> ret = defaultValue;
    	
    	JsonElement element = json.get(memberName);
    	
    	if(element!=null)
    	{
    		JsonArray array = element.getAsJsonArray();
    		if(array!=null)
    		{
    			ArrayList<T> retnew = new ArrayList<T>();
    			
    			for(JsonElement e:array)
    			{
    				SFODataObject object = SFDefaultGsonParser.parse(clazz, e);
    				retnew.add((T) object);
    			}
    			
    			ret = retnew;
    		}
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
				ret = SFDefaultGsonParser.parse(clazz,element); 
			} 
    		catch (Exception e) 
    		{				
				e.printStackTrace();
			}     		    		    		    				
    	}    	
    	
    	return ret;
    }
    
    public static SFItem parseSFItem(JsonObject jsonObject)	
	{					
		SFItem item = new SFItem();
		
		item.setMetadata(SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_METADATA, null));
		item.seturl(SFGsonHelper.getURI(jsonObject, SFKeywords.URL, null));
		item.setId(SFGsonHelper.getString(jsonObject, SFKeywords.Id, null));
		
		return item;
	}
	
    public static SFODataFeed<SFODataObject> parseFeed(Class clazz,JsonObject jsonObject)	
	{					
		SFODataFeed<SFODataObject> item = new SFODataFeed<SFODataObject>();
		
		item.setMetadata(SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_METADATA, null));
		item.seturl(SFGsonHelper.getURI(jsonObject, SFKeywords.URL, null));
		item.setId(SFGsonHelper.getString(jsonObject, SFKeywords.Id, null));
		
		int count = SFGsonHelper.getInt(jsonObject, SFKeywords.ODATA_COUNT, 0);
		item.setcount(count);
		item.setNextLink(SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_NEXTLINK, null));
						
		ArrayList<SFODataObject> Feed = SFGsonHelper.getArrayList(clazz, jsonObject, SFKeywords.VALUE, null);
				
		item.setFeed(Feed);
		
		return item;
	}    	
    
    /**
     *   This function finds type of SFODataObject (including feed types!!) from the metadata and then calls the default gson parser
     *   for the appropriate class type contained in the json string.
     *   
     *   <p>This function can correctly parse only objects with a valid  "odata.metadata" that can be mapped to one of the SFV3Element or FeedTypes
     *   .The function will return null otherwise.
     */
    public static SFODataObject customParse(JsonElement jsonElement)
	{
		SFODataObject ret = null;
		
		try
		{
			if(jsonElement!=null)
			{
				
				SFLog.d2(TAG,"Route for %s" +  jsonElement.toString());
				
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				
				if(jsonObject!=null)
				{
					String metadata = SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_METADATA, null);
					
					SFV3ElementType elementType = SFV3ElementType.getElementTypeFromMetaData(metadata);
																														
					if(elementType!=null)
					{
						SFLog.d2(TAG, "GSON For : %s", metadata);
						
						switch (elementType) 
						{
							case Item:		
								/*
								 *  This needs explicit parsing to avoid going into infinite recursion and stackoverflow
								 *  when enumerating folders.
								 */
								ret = SFGsonHelper.parseSFItem(jsonObject);
							break;

						default:
							ret = SFDefaultGsonParser.parse(elementType.getV3Class(), jsonElement);
							break;
						}						
					}
					else
					{
						SFV3FeedType feedType = SFV3FeedType.getFeedTypeFromMetaData(metadata);
						
						if(feedType!=null)
						{
							SFLog.d2(TAG, "GSON For : %s", metadata);
							ret = SFGsonHelper.parseFeed(feedType.getV3Class(), jsonObject);
						}
					}										
				}
				else
				{
					SFLog.d2(TAG,"JSON Object NULL");
				}
			}
			else
			{
				SFLog.d2(TAG,"JSON Element NULL");
			}
		}
		catch(Exception e)
		{									
			SFLog.d2(TAG,"Exception MSG = %s"  , Log.getStackTraceString(e));
		}
		
		if(ret ==null)
		{
			SFLog.d2(TAG,"Returning null  ");
		}
		else
		{
			if(ret instanceof SFFile)
			{
				SFLog.d2(TAG,"Returning NON null  %s" , ((SFFile)ret).getName());
			}
		}
		
		return ret;
	}
}