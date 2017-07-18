package com.citrix.sharefile.api.gson.auto;

import com.citrix.sharefile.api.models.SFODataObject;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 *   This class read the odata.metadata from the JsonElement to find out the real type of object contained inside the object 
 *   and the routes the parsing back to the correct default gson parser.
 */
public class SFGsonRouter implements JsonDeserializer<SFODataObject>
{
	private final SFDefaultGsonParser gsonParser;

	public SFGsonRouter(SFDefaultGsonParser gsonParser)
	{
		this.gsonParser = gsonParser;
	}

	@Override
	public SFODataObject deserialize(JsonElement jsonElement, Type typeOfObject,JsonDeserializationContext desContext) throws JsonParseException 
	{
		return gsonParser.customParse(jsonElement, typeOfObject);
	}
}