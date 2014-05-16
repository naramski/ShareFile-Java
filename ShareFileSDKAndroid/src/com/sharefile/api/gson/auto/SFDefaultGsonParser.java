package com.sharefile.api.gson.auto;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.sharefile.api.SFV3Error;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFODataFeed;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFPrincipal;
import com.sharefile.java.log.SLog;

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
			
	public static <T> SFODataObject parse(Class<?> clazz,JsonElement jsonElement)	
	{		
		return (SFODataObject) getInstance().mGson.fromJson(jsonElement, clazz);		
	}	
			
	public static SFV3Error parse(JsonElement jsonElement)	
	{		
		return getInstance().mGson.fromJson(jsonElement, SFV3Error.class);		
	}
	
	public static String serialize(Type clazz,Object src)	
	{		
		return getInstance().mGson.toJson(src, clazz);		
	}
	
			
        	
	/**
	 *  Certain classes like SFPrincipal can't rely on the default gson parsing since we need to get the contained inner object
	 *  in them using the odata.metatata and then handover the gson parsing to actual class contained in SFPrincipal.
	 *  <p>This is particulalry true of objects contained inside a feed. exampple a folder feed has type ArrayList<SFItem>;
	 *  So gson will try to parse the objects inside a feed using the parser for SFItem.class but we need them to be parsed using
	 *  the individual SFFile,SFLink,SFFoler,SFLink etc classes. We re-pass the SFItem to SFGsonRouter. Note how we have avoided 
	 *  self-recursion inside the SFGsonRouter.
	 *  </p> 
	 * 
	 * 
	 * V3Date Format is: ", ;//yyyy-MM-dd'T'HH:mm:ss.SSSZ
	 */
	
	private final  SimpleDateFormat v3SimpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSSZ");
	
	private void registerSFSpecificGsonAdapters()
	{		
		mGsonBuilder.registerTypeAdapter(SFPrincipal.class, new SFGsonRouter());
		mGsonBuilder.registerTypeAdapter(SFItem.class, new SFGsonRouter());
		mGsonBuilder.registerTypeAdapter(SFODataFeed.class, new SFGsonRouter());
				
		mGsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() 
		{

			@Override
			public Date deserialize(JsonElement arg0, Type arg1,JsonDeserializationContext arg2) throws JsonParseException 
			{				
				Date date = null;
				try 
				{
					date = v3SimpleDateFormat.parse(arg0.getAsString().replace("Z", "+0000"));
					//SLog.d("pdate", "got date");			
				} 
				catch (Exception e) 
				{				
					SLog.d("pdate", "date-parse",e);					
				}
				
				return date;
			}
		});
	}		
}