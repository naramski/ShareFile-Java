package com.citrix.sharefile.api.gson.auto;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import com.citrix.sharefile.api.enumerations.SFSafeEnum;
import com.citrix.sharefile.api.enumerations.SFSafeEnumFlags;
import com.citrix.sharefile.api.models.SFItem;
import com.citrix.sharefile.api.models.SFItemInfo;
import com.citrix.sharefile.api.models.SFODataFeed;
import com.citrix.sharefile.api.models.SFODataObject;
import com.citrix.sharefile.api.models.SFPrincipal;
import com.citrix.sharefile.api.models.SFShare;
import com.citrix.sharefile.api.models.SFStorageCenter;
import com.citrix.sharefile.api.utils.SFDateFormat;
import com.citrix.sharefile.api.utils.SafeEnumHelpers;

/**
 *   This class goes for the default gson parsing for most common objects. For objects 
 *   that need explicit routing to a different parser class (example the SFPrincipal class) it relies on the class specific parser     
 */
public class SFDefaultGsonParser 
{	
	private static final GsonBuilder mGsonBuilder = new GsonBuilder();
	private final Gson mGson;
	private static SFDefaultGsonParser mInstance = null; 
	
	SFDefaultGsonParser()
	{
		registerSFSpecificGsonAdapters();
		mGson = mGsonBuilder.setDateFormat("yyyy-MM-dd").create();
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
        mGsonBuilder.registerTypeAdapter(SFSafeEnum.class, new SFCustomSafeEnumParser());
        mGsonBuilder.registerTypeAdapter(SFSafeEnumFlags.class, new SFCustomSafeEnumFlagsParser());

		mGsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() 
		{
			@Override
			public Date deserialize(JsonElement arg0, Type arg1,JsonDeserializationContext arg2) throws JsonParseException 
			{				
				return SFDateFormat.parse(arg0.getAsString());
			}
		});
		
		mGsonBuilder.registerTypeAdapter(URI.class, new JsonDeserializer<URI>() 
		{
			@Override
			public URI deserialize(JsonElement arg0, Type arg1,JsonDeserializationContext arg2) throws JsonParseException
			{											
				try 
				{
					return new URI (arg0.getAsString().trim());
				} 
				catch (URISyntaxException e) 
				{
					throw new JsonParseException(e);
				}				
			}
		});		
	}		
}