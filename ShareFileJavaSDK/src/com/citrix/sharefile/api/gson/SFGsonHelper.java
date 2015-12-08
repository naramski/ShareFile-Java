package com.citrix.sharefile.api.gson;

import com.citrix.sharefile.api.constants.SFKeywords;
import com.citrix.sharefile.api.enumerations.SFV3ElementType;
import com.citrix.sharefile.api.gson.auto.SFDefaultGsonParser;
import com.citrix.sharefile.api.log.Logger;
import com.citrix.sharefile.api.models.SFEntityTypeMap;
import com.citrix.sharefile.api.models.SFItem;
import com.citrix.sharefile.api.models.SFODataFeed;
import com.citrix.sharefile.api.models.SFODataObject;
import com.citrix.sharefile.api.models.SFStorageCenter;
import com.citrix.sharefile.api.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

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
    
    public static <T> ArrayList<T> getArrayList(Class<?> clazz, JsonObject json,String memberName,ArrayList<T> defaultValue)
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
        
    public static SFODataObject getSFODataObject(Class<?> clazz,JsonObject json,String memberName,SFODataObject defaultValue)
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
    			Logger.e(TAG,e);
			}     		    		    		    				
    	}    	
    	
    	return ret;
    }
    
    public static SFItem parseSFItem(JsonObject jsonObject)	
	{					
		SFItem item = null;
		
		try 
		{
			// note we are creating the override class registered by the app instead of the hardcoding:  new SFItem()
			item = (SFItem) SFV3ElementType.Item.getV3Class().newInstance();
			item.setName(SFGsonHelper.getString(jsonObject, SFKeywords.Name, null));
			item.setFileName(SFGsonHelper.getString(jsonObject, SFKeywords.FileName, null));			
			item.setMetadataUrl(SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_METADATA, null));
			item.seturl(SFGsonHelper.getURI(jsonObject, SFKeywords.URL, null));
			item.setId(SFGsonHelper.getString(jsonObject, SFKeywords.Id, null));		
		} 			
		//None of these exceptions should ideally happen since we have done all the checks in registerSubClass()			
		catch (InstantiationException e) 
		{	
			Logger.e(TAG,e);
		} 
		catch (IllegalAccessException e) 
		{				
			Logger.e(TAG,e);
		}		 
		 				
		return item;
	}
    
    public static SFStorageCenter parseSFStorageCenter(JsonObject jsonObject)	
   	{					
   		SFStorageCenter sc = null;
   		
   		try 
   		{
   			// note we are creating the override class registered by the app instead of the hardcoding:  new SFItem()
   			sc = (SFStorageCenter) SFV3ElementType.StorageCenter.getV3Class().newInstance();
   			sc.setExternalAddress(SFGsonHelper.getString(jsonObject, SFKeywords.EXTERNAL_ADDRESS, null));
   			sc.setExternalUrl(SFGsonHelper.getString(jsonObject, SFKeywords.EXTERNAL_URL, null));
   			sc.setDefaultExternalUrl(SFGsonHelper.getString(jsonObject, SFKeywords.DEFAULT_EXTERNAL_URL, null));
   			sc.setMetadataUrl(SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_METADATA, null));
   			sc.seturl(SFGsonHelper.getURI(jsonObject, SFKeywords.URL, null));
   			sc.setId(SFGsonHelper.getString(jsonObject, SFKeywords.Id, null));		
   		} 			
   		//None of these exceptions should ideally happen since we have done all the checks in registerSubClass()			
   		catch (InstantiationException e) 
   		{	
   			Logger.e(TAG,e);
   		} 
   		catch (IllegalAccessException e) 
   		{				
   			Logger.e(TAG,e);
   		}		 
   		 				
   		return sc;
   	}
	
    public static SFODataFeed<SFODataObject> parseFeed(Class<?> clazz,JsonObject jsonObject)	
	{					
		SFODataFeed<SFODataObject> item = new SFODataFeed<SFODataObject>();
		
		item.setMetadataUrl(SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_METADATA, null));
		item.seturl(SFGsonHelper.getURI(jsonObject, SFKeywords.URL, null));
		item.setId(SFGsonHelper.getString(jsonObject, SFKeywords.Id, null));
		
		int count = SFGsonHelper.getInt(jsonObject, SFKeywords.ODATA_COUNT, 0);
		item.setcount(count);
		item.setNextLink(SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_NEXTLINK, null));
						
		ArrayList<SFODataObject> Feed = SFGsonHelper.getArrayList(clazz, jsonObject, SFKeywords.VALUE, null);
		item.setFeed(Feed);
		
		return item;
	}    	
    
	private static final String SHAREFILE_MODELS_PACKAGE_NAME = "ShareFile.Api.Models.";
	private static final String ELEMENT_TAG = "@Element";
	private static final String METADATA_FEED_TAG = "$metadata#";

	private static String replaceLeftSide(String tag, String original)
	{
		int tagIndex = original.lastIndexOf(tag);

		if(tagIndex > -1)
		{
			return original.substring(tagIndex + tag.length());
		}

		return original;
	}

	/*
	 * This function will get the base bones class name from odata.type or odata.metadata string.
	 *
	 * Sample:
	 *
	 * odata.metadata : https://subdomain.sf-api.com/sf/v3/$metadata#Capabilities
	 * odata.type     : ShareFile.Api.Models.Folder
	 *
	 * odata.metadata : https://enttest1.sf-api.com/sf/v3/$metadata#Items/ShareFile.Api.Models.Folder@Element
	 *
	 *
	 * For SharePoint Feed we get the following strange combination:
	 * odata.metadata	:	https://szqatest2.sharefiletest.com/sp/v3/$metadata#Capabilities
	   odata.type	:	ShareFile.Api.Models.ODataFeed`1[[ShareFile.Api.Models.Capability, ShareFile.Api.Models, Version=1.0.0.0, Culture=neutral, PublicKeyToken=null]]

	 */
	private static String getElementName(String original)
	{
		String ret = original.replace(ELEMENT_TAG, "");
		ret = replaceLeftSide(SHAREFILE_MODELS_PACKAGE_NAME,ret);
		ret = replaceLeftSide(METADATA_FEED_TAG,ret);
		return ret.trim();
	}

	/*
	 *  This function will get the element from the EntityTypeMap
	 */
	private static Class getClassFromString(String str)
	{
		Class clazz = SFEntityTypeMap.getEntityTypeMap().get(getElementName(str));
		if(clazz == null)
		{
			Logger.d(TAG,"Object not in EntityMap: " + str);
			return null;
		}
		return clazz;
	}

	private static SFODataObject parseSFElement(Class clazz, JsonObject jsonObject)
	{
		if(SFItem.class.getCanonicalName().equals(clazz.getCanonicalName()))
		{
			// This needs explicit parsing to avoid going into infinite recursion and stackoverflow
			//when enumerating folders.
			return SFGsonHelper.parseSFItem(jsonObject);
		}

		if(SFStorageCenter.class.getCanonicalName().equals(clazz.getCanonicalName()))
		{
			return SFGsonHelper.parseSFStorageCenter(jsonObject);
		}

		return SFDefaultGsonParser.parse(clazz, jsonObject);
	}

	private static SFODataObject parseJsonObject(String odata, JsonObject jsonObject, boolean isFeed)
	{
		Class clazz = getClassFromString(odata);
		if(clazz == null)
		{
			return null;
		}

		if(!isFeed)
		{
			return parseSFElement(clazz, jsonObject);
		}

		return parseFeed(clazz,jsonObject);
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
		try
		{
			if(jsonElement == null)
			{
				Logger.d(TAG,"JSON Element NULL");
				return null;
			}

			JsonObject jsonObject = jsonElement.getAsJsonObject();
				
			if(jsonObject == null)
			{
				Logger.d(TAG,"JSON Object NULL");
				return null;
			}

			String odataType = SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_TYPE, null);
			String metadata = SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_METADATA, null);

			if(odataType ==null)
			{
				if(metadata.endsWith(ELEMENT_TAG))
				{
					odataType = metadata;
				}
			}

			if(odataType != null) //Element is non-feed type
			{
				SFODataObject ret = parseJsonObject(odataType,jsonObject,false);

				if(ret!=null)
				{
					return ret;
				}

				//Got a null object implies the server added a new type of Object which the SDK
				//Has no idea about or we got an anomalous type like the SharePoint Capabilities feed.
				//Before parsing this as a feed make sure that it is a FEED
				if(metadata == null || !metadata.contains(METADATA_FEED_TAG))
				{
					Logger.e(TAG,"Upgrade the SDK. No object type for: " + odataType);
					return null;
				}
			}

			//Feed type element

			return parseJsonObject(metadata, jsonObject, true);

		}
		catch(Exception e)
		{									
			Logger.e(TAG, e);
		}

		return null;
	}
}