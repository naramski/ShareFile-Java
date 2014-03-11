package com.sharefile.api.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.sharefile.api.SFModelFactory;
import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.exceptions.SFJsonException;
import com.sharefile.api.models.SFODataObject;

public class SFGsonParser implements JsonDeserializer<SFODataObject>, JsonSerializer<SFODataObject>
{	
	public static void parseFromJson(SFODataObject object,String jsonString) throws SFJsonException
	{
		try
        {        			
			Type t = object.getClass();
        	SFGsonHelper.fromJson(jsonString, object.getClass(), SFModelFactory.getTypeTokenFromClassName(object.getClass().getName()), new SFGsonParser());
        }
        catch(JsonSyntaxException ex)
        {
        	throw new SFJsonException(ex);
        }
	}
	
	public static SFODataObject parseFromJson(Class clazz,String jsonString) throws SFJsonException
	{
		try
        {        		
			return SFGsonHelper.fromJson(jsonString, clazz, SFModelFactory.getTypeTokenFromClassName(clazz.getName()), new SFGsonParser());
        }
        catch(JsonSyntaxException ex)
        {
        	throw new SFJsonException(ex);
        }
	}
	
	@Override
	public SFODataObject deserialize(JsonElement jsonElement, Type typeOfObject,JsonDeserializationContext desContext) throws JsonParseException 
	{		
		SFLog.d2("GSON", "deserialize class: %s", typeOfObject.getClass().getName());
		return null;
	}

	@Override
	public JsonElement serialize(SFODataObject sfODataObject, Type typeOfObject,JsonSerializationContext serContext) 
	{		
		return null;
	}		
}