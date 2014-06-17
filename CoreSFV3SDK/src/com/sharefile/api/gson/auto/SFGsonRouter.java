package com.sharefile.api.gson.auto;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sharefile.api.gson.SFGsonHelper;
import com.sharefile.api.models.SFItem;
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
		return SFGsonHelper.customParse(jsonElement);
	}

	@Override
	public JsonElement serialize(SFODataObject sfODataObject, Type typeOfObject,JsonSerializationContext serContext) 
	{	
		JsonElement ret = null;
		String str = null;
		
		if(sfODataObject instanceof SFItem) {
			str = "{\"Id\":\"" + sfODataObject.getId() + "\"}";
		}
		else {
		str = SFDefaultGsonParser.serialize(typeOfObject, sfODataObject);
		}
		
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