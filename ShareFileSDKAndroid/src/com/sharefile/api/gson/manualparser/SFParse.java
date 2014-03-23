package com.sharefile.api.gson.manualparser;

import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.gson.SFGsonHelper;
import com.sharefile.api.models.SFAccessControl;
import com.sharefile.api.models.SFAccountUser;
import com.sharefile.api.models.SFCapability;
import com.sharefile.api.models.SFFile;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFLink;
import com.sharefile.api.models.SFNote;
import com.sharefile.api.models.SFODataFeed;
import com.sharefile.api.models.SFSession;

public class SFParse 
{
		
	static SFSession parse(SFSession object,JsonObject jsonObject)	
	{
		return object;
	}
	
	static SFFile parse(SFFile object,JsonObject jsonObject)	
	{
		return object;
	}
	
	static SFFolder parse(SFFolder object,JsonObject jsonObject)	
	{
		return object;
	}
	
	static SFNote parse(SFNote object,JsonObject jsonObject)	
	{
		return object;
	}
		
	static SFLink parse(SFLink object,JsonObject jsonObject)	
	{
		return object;
	}
	
	static SFAccountUser parse(SFAccountUser object,JsonObject jsonObject)	
	{							  
		return object;
	}
	
	public static SFItem parseSFItem(JsonObject jsonObject)	
	{					
		SFItem item = new SFItem();
		
		item.setMetadata(SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_METADATA, null));
		item.seturl(SFGsonHelper.getURI(jsonObject, SFKeywords.URL, null));
		item.setId(SFGsonHelper.getString(jsonObject, SFKeywords.Id, null));
		
		return item;
	}
	
	public static SFODataFeed<SFCapability> parseCapabilityFeed(JsonObject jsonObject)	
	{					
		SFODataFeed<SFCapability> item = new SFODataFeed<SFCapability>();
		
		item.setMetadata(SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_METADATA, null));
		item.seturl(SFGsonHelper.getURI(jsonObject, SFKeywords.URL, null));
		item.setId(SFGsonHelper.getString(jsonObject, SFKeywords.Id, null));
		
		int count = SFGsonHelper.getInt(jsonObject, SFKeywords.ODATA_COUNT, 0);
		item.setcount(count);
		item.setNextLink(SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_NEXTLINK, null));
						
		ArrayList<SFCapability> Feed = SFGsonHelper.getArrayList(SFCapability.class, jsonObject, SFKeywords.VALUE, null);
				
		item.setFeed(Feed);
		
		return item;
	}
	
	
	public static SFODataFeed<SFAccessControl> parseAccessControlFeed(JsonObject jsonObject)	
	{					
		SFODataFeed<SFAccessControl> item = new SFODataFeed<SFAccessControl>();
		
		item.setMetadata(SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_METADATA, null));
		item.seturl(SFGsonHelper.getURI(jsonObject, SFKeywords.URL, null));
		item.setId(SFGsonHelper.getString(jsonObject, SFKeywords.Id, null));
		
		int count = SFGsonHelper.getInt(jsonObject, SFKeywords.ODATA_COUNT, 0);
		item.setcount(count);
		item.setNextLink(SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_NEXTLINK, null));
						
		ArrayList<SFAccessControl> Feed = SFGsonHelper.getArrayList(SFAccessControl.class, jsonObject, SFKeywords.VALUE, null);
				
		item.setFeed(Feed);
		
		return item;
	}
	
	/**
	 * Add all new parse functions in the similar pattern
	 * <p> Note they should have package scope. Can't use Java generics coz of the problem with Java generics and erasures
	 * 
	    <T> parse(<T> object,JsonObject jsonObject)	
		{
			return object;
		} 		
	*/
}
