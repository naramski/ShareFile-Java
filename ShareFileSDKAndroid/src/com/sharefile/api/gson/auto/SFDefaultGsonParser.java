package com.sharefile.api.gson.auto;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFPrincipal;
import com.sharefile.api.models.SFSession;

/**
 *   This class goes for the default gson parsing for most common objects. For objects 
 *   that need explicit routing to a different parser class (example the SFPrincipal class) it relies on the class specific parser     
 */
public class SFDefaultGsonParser 
{	
	private final GsonBuilder mGsonBuilder;
	private final Gson mGson;
	private static SFDefaultGsonParser mInstance = null; 
	
	SFDefaultGsonParser()
	{		
		mGsonBuilder = new GsonBuilder();				
		registerSFSpecificGsonAdapters();
		mGson = mGsonBuilder.create();
	}
	
	public static SFDefaultGsonParser getInstance()
	{
		if(mInstance == null )
		{
			mInstance = new SFDefaultGsonParser();
		}
		
		return mInstance;
	}
			
	public static <T> SFODataObject parse(Class clazz,JsonElement jsonElement)	
	{		
		return (SFODataObject) getInstance().mGson.fromJson(jsonElement, clazz);		
	}	
			
	public static String serialize(Type clazz,Object src)	
	{		
		return getInstance().mGson.toJson(src, clazz);		
	}
				
	/**
	 *  Certain classes like SFPrincipal can't rely on the default gson parsing since we need to get the contained inner object
	 *  in them using the odata.metatata and then handover the gson parsing to actual class contained in SFPrincipal
	 */
	private void registerSFSpecificGsonAdapters()
	{
		mGsonBuilder.registerTypeAdapter(SFPrincipal.class, new SFGsonRouter());
		mGsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() 
		{

			@Override
			public Date deserialize(JsonElement arg0, Type arg1,JsonDeserializationContext arg2) throws JsonParseException 
			{				
				return null;
			}
		});
	}
		
}