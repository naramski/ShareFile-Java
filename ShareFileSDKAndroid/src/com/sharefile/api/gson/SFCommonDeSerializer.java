package com.sharefile.api.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.models.SFODataObject;

public class SFCommonDeSerializer implements JsonDeserializer<SFODataObject> 
{
	@Override
	public SFODataObject deserialize(JsonElement jsonElement, Type typeOfObject,JsonDeserializationContext desContext) throws JsonParseException 
	{		
		SFLog.d2("GSON", "deserialize class: %s", typeOfObject.getClass().getName());
		return null;
	}			 	
}