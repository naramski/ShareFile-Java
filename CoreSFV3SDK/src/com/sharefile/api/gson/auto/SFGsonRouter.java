package com.sharefile.api.gson.auto;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sharefile.api.gson.SFGsonHelper;
import com.sharefile.api.models.SFGroup;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFUser;

/**
 *   This class read the odata.metadata from the JsonElement to find out the real type of object contained inside the object 
 *   and the routes the parsing back to the correct default gson parser.
 */
public class SFGsonRouter implements JsonDeserializer<SFODataObject>, JsonSerializer<SFODataObject>
{		
	private static final String TAG = "SFGsonRouter";
	
	@Override
	public SFODataObject deserialize(JsonElement jsonElement, Type typeOfObject,JsonDeserializationContext desContext) throws JsonParseException 
	{		
		return SFGsonHelper.customParse(jsonElement);
	}

	@Override
	public JsonElement serialize(SFODataObject sfODataObject, Type typeOfObject,JsonSerializationContext serContext) 
	{	
		JsonElement ret = null;		
						
		if(sfODataObject instanceof SFItem) 
		{
			ret = SFCustomSerializer.serialize((SFItem)sfODataObject);
		}
		else if(sfODataObject instanceof SFUser) 
		{			
			ret = SFCustomSerializer.serialize((SFUser)sfODataObject);
		}
		else if(sfODataObject instanceof SFGroup) 
		{			
			ret = SFCustomSerializer.serialize((SFGroup)sfODataObject);
		}
		else 
		{
			ret = SFCustomSerializer.serialize(sfODataObject, typeOfObject);
		}
									
		return ret;
	}					
}