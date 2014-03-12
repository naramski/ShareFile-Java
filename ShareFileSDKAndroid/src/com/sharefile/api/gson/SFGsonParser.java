package com.sharefile.api.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.sharefile.api.SFModelFactory;
import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.exceptions.SFJsonException;
import com.sharefile.api.exceptions.SFToDoReminderException;
import com.sharefile.api.models.SFAccountUser;
import com.sharefile.api.models.SFFile;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFLink;
import com.sharefile.api.models.SFNote;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSession;

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
		//return callClassSpecificParser(jsonElement, typeOfObject);
		return callDefaultGsonParser(jsonElement, typeOfObject);
	}

	@Override
	public JsonElement serialize(SFODataObject sfODataObject, Type typeOfObject,JsonSerializationContext serContext) 
	{		
		SFToDoReminderException.throwTODOException("need to implement serialize for element type : " + sfODataObject.getClass().getName());
		
		return null;
	}
		
	private SFODataObject callClassSpecificParser(JsonElement jsonElement, Type typeOfObject)
	{
		SFODataObject ret = null;
		SFLog.d2("GSON", "deserialize class: %s", typeOfObject.getClass().getName());
		
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		
		JsonElement sfElementType = jsonObject.get(SFKeywords.ODATA_METADATA);
		
		if(sfElementType==null)
		{
			return null;
		}
		
		String metatdata = sfElementType.toString();
				
		SFV3ElementType type = SFModelFactory.getElementTypeFromMetaData(metatdata);
		
		switch (type) 
		{								        
			case File:				
				ret = SFClassSpecificGsonParser.parse(new SFFile(), jsonObject);
			break;

			case Folder:				
				ret = SFClassSpecificGsonParser.parse(new SFFolder(), jsonObject);				
			break;
			
			case Link:				
				ret = SFClassSpecificGsonParser.parse(new SFLink(), jsonObject);
			break;
			
			case Note:				
				ret = SFClassSpecificGsonParser.parse(new SFNote(), jsonObject);
			break;
			
			case Session:
				ret = SFClassSpecificGsonParser.parse(new SFSession(), jsonObject);
			break;
			
			case AccountUser:
				ret = SFClassSpecificGsonParser.parse(new SFAccountUser(),jsonObject);
			break;
			
			default:
				SFToDoReminderException.throwTODOException("need to implement parsing for element type : " + type.toString());
				ret = null;
			break;
		}
		
		return ret;
	}
	
	
	private SFODataObject callDefaultGsonParser(JsonElement jsonElement, Type typeOfObject)
	{
		SFODataObject ret = null;
		SFLog.d2("GSON", "deserialize class: %s", typeOfObject.getClass().getName());
		
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		
		JsonElement sfElementType = jsonObject.get(SFKeywords.ODATA_METADATA);
		
		if(sfElementType==null)
		{
			return null;
		}
		
		String metatdata = sfElementType.toString();
				
		SFV3ElementType type = SFModelFactory.getElementTypeFromMetaData(metatdata);
		
		switch (type) 
		{								        
			case File:				
				ret = SFDefaultGsonParser.parse(SFFile.class, jsonElement);
			break;

			case Folder:				
				ret = SFDefaultGsonParser.parse(SFFolder.class, jsonElement);				
			break;
			
			case Link:				
				ret = SFDefaultGsonParser.parse(SFLink.class, jsonElement);
			break;
			
			case Note:				
				ret = SFDefaultGsonParser.parse(SFNote.class, jsonElement);
			break;
			
			case Session:
				ret = SFDefaultGsonParser.parse(SFSession.class, jsonElement);
			break;
			
			case AccountUser:
				ret = SFDefaultGsonParser.parse(SFAccountUser.class, jsonElement);
			break;
			
			default:
				//SFToDoReminderException.throwTODOException("need to implement parsing for element type : " + type.toString());
				ret = null;
			break;
		}
		
		return ret;
	}
}