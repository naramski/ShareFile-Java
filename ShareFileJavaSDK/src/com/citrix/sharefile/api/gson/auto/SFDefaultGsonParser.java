package com.citrix.sharefile.api.gson.auto;

import com.citrix.sharefile.api.constants.SFKeywords;
import com.citrix.sharefile.api.gson.SFGsonHelper;
import com.citrix.sharefile.api.log.Logger;
import com.citrix.sharefile.api.models.SFEntityTypeMap;
import com.citrix.sharefile.api.models.SFODataFeed;
import com.citrix.sharefile.api.models.SFODataObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.ArrayList;


/**
 *   This class goes for the default gson parsing for most common objects. For objects 
 *   that need explicit routing to a different parser class (example the SFPrincipal class) it relies on the class specific parser     
 */
public class SFDefaultGsonParser 
{
	private static final String TAG = "SFGsonParser";
	private static final String SHAREFILE_MODELS_PACKAGE_NAME = "ShareFile.Api.Models.";
	private static final String ELEMENT_TAG = "@Element";
	private static final String METADATA_FEED_TAG = "$metadata#";
	private static final String REDIRECTION_MODEL_TAG = SHAREFILE_MODELS_PACKAGE_NAME+"Redirection";
	private Gson mGson;
	
	public SFDefaultGsonParser()
	{
		mGson = SFGsonBuilder.build(null,this);
	}

	private void ignoreClassFromSFGsonRouter(Type classToIgnoreFromSFGsonRouting)
	{
		mGson = SFGsonBuilder.build(classToIgnoreFromSFGsonRouting,this);
	}


	private <T> SFODataObject parse(Class<?> clazz,JsonElement jsonElement)
	{		
		return (SFODataObject) mGson.fromJson(jsonElement, clazz);
	}	
			

	public String serialize(Type clazz,Object src)
	{		
		return mGson.toJson(src, clazz);
	}

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
	 * This function will get the bare bones class name from odata.type or odata.metadata string.
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

	private SFODataObject parseSFElement(Class clazz, JsonObject jsonObject)
	{
		return parse(clazz, jsonObject);
	}

	private SFODataObject parseJsonObject(String odata, JsonObject jsonObject, boolean isFeed, Type typeOfObjectGuessedByGson)
	{
		Class clazz = getClassFromString(odata);
		if(clazz == null)
		{
			return null;
		}

		boolean resetGson = false;

		if(SFGsonBuilder.isRegisteredForInternalGsonRouting(clazz)){
			if(typeOfObjectGuessedByGson == clazz){
				Logger.d(TAG, "Entity Map and Gson Guess Match. Temporary Unregister: " + typeOfObjectGuessedByGson.toString() + " from internal routing");
				ignoreClassFromSFGsonRouter(clazz);
				resetGson = true;
			}
		}

		try {
			if (!isFeed) {
				return parseSFElement(clazz, jsonObject);
			}

			return parseFeed(clazz, jsonObject);
		}
		finally {
			if(resetGson){
				ignoreClassFromSFGsonRouter(null);
			}
		}
	}

	private SFODataFeed<SFODataObject> parseFeed(Class<?> clazz, JsonObject jsonObject)
	{
		SFODataFeed<SFODataObject> item = new SFODataFeed<SFODataObject>();

		item.setMetadataUrl(SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_METADATA, null));
		item.seturl(SFGsonHelper.getURI(jsonObject, SFKeywords.URL, null));
		item.setId(SFGsonHelper.getString(jsonObject, SFKeywords.Id, null));

		int count = SFGsonHelper.getInt(jsonObject, SFKeywords.ODATA_COUNT, 0);
		item.setcount(count);
		item.setNextLink(SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_NEXTLINK, null));

		ArrayList<SFODataObject> Feed = getArrayList(clazz, jsonObject, SFKeywords.VALUE, null);
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
	public SFODataObject customParse(JsonElement jsonElement, Type typeOfObjectGuessedByGson)
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
				//if metadata ends with element tag or redirection tag, never parse it as FeedType.
				if(metadata.endsWith(ELEMENT_TAG) || metadata.endsWith(REDIRECTION_MODEL_TAG))
				{
					odataType = metadata;
				}
			}

			if(odataType != null) //Element is non-feed type
			{
				SFODataObject ret = parseJsonObject(odataType,jsonObject,false,typeOfObjectGuessedByGson);

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

			return parseJsonObject(metadata, jsonObject, true,typeOfObjectGuessedByGson);

		}
		catch(Exception e)
		{
			Logger.e(TAG, e);
		}

		return null;
	}

	private <T> ArrayList<T> getArrayList(Class<?> clazz, JsonObject json,String memberName,ArrayList<T> defaultValue)
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
					SFODataObject object = parse(clazz, e);
					if (object==null) {
						Logger.e(TAG, new Exception("NULL Object in feed: " + ((e!=null) ? e.toString() : "NULL element")));
						continue;
					}
					retnew.add((T) object);
				}

				ret = retnew;
			}
		}

		return ret;
	}
}