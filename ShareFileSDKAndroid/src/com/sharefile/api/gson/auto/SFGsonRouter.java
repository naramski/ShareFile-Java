package com.sharefile.api.gson.auto;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sharefile.api.SFModelFactory;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.exceptions.SFToDoReminderException;
import com.sharefile.api.gson.SFGsonHelper;
import com.sharefile.api.models.SFAccountUser;
import com.sharefile.api.models.SFFile;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFLink;
import com.sharefile.api.models.SFNote;
import com.sharefile.api.models.SFODataObject;

/**
 *   This class read the odata.metadata from the JsonElement to find out the real type of object contained inside the object 
 *   and the routes the parsing back to the correct defuault gson parser.
 */
public class SFGsonRouter implements JsonDeserializer<SFODataObject>, JsonSerializer<SFODataObject>
{		
	@Override
	public SFODataObject deserialize(JsonElement jsonElement, Type typeOfObject,JsonDeserializationContext desContext) throws JsonParseException 
	{		
		SFODataObject ret = null;
		
		if(jsonElement!=null)
		{
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			
			if(jsonObject!=null)
			{
				String metadata = SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_METADATA, null);
				SFV3ElementType elementType = SFModelFactory.getElementTypeFromMetaData(metadata);
				
				switch(elementType)
				{
					case AccountUser:
						ret = SFDefaultGsonParser.parse(SFAccountUser.class, jsonElement);
					break;
					
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
					
					default:
						SFToDoReminderException.throwTODOException("Need to implement parser for : " + elementType.toString());
					break;	
				}
			}						
		}
		
		return ret;
	}

	@Override
	public JsonElement serialize(SFODataObject sfODataObject, Type typeOfObject,JsonSerializationContext serContext) 
	{	
		JsonElement ret = null;
		String str = null;
		
		str = SFDefaultGsonParser.serialize(typeOfObject, sfODataObject);
		
		if(str!=null)
		{
			JsonParser parser = new JsonParser();
			ret = parser.parse(str);
		}	
		
		/*
		SFV3ElementType elementType = SFModelFactory.getElementTypeClassName(typeOfObject.getClass().getName());
		
		switch(elementType)
		{
			case AccountUser:				
				str = SFDefaultGsonParser.serialize(SFAccountUser.class, sfODataObject);				
			break;
			
			case File:
				str = SFDefaultGsonParser.serialize(SFFile.class, sfODataObject);
			break;
			
			case Folder:
				str = SFDefaultGsonParser.serialize(SFFolder.class, sfODataObject);
			break;
			
			case Link:
				str = SFDefaultGsonParser.serialize(SFLink.class, sfODataObject);
			break;
			
			case Note:
				str = SFDefaultGsonParser.serialize(SFNote.class, sfODataObject);
			break;
			
			default:
				SFToDoReminderException.throwTODOException("Need to implement parser for : " + elementType.toString());
			break;	
		}*/
		
		return ret;
	}			
}