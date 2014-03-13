package com.sharefile.api.gson.auto;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sharefile.api.SFModelFactory;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.exceptions.SFToDoReminderException;
import com.sharefile.api.gson.SFGsonHelper;
import com.sharefile.api.models.SFAccountUser;
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
					
					default:
					break;	
				}
			}
						
		}
		
		return ret;
	}

	@Override
	public JsonElement serialize(SFODataObject sfODataObject, Type typeOfObject,JsonSerializationContext serContext) 
	{		
		SFToDoReminderException.throwTODOException("need to implement serialize for element type : " + sfODataObject.getClass().getName());
		
		return null;
	}			
}