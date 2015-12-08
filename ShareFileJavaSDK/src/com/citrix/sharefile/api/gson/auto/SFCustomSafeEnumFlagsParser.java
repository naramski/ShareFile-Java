package com.citrix.sharefile.api.gson.auto;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.citrix.sharefile.api.enumerations.SFSafeEnumFlags;
import com.citrix.sharefile.api.utils.SafeEnumHelpers;

import java.lang.reflect.Type;

/**
 *   This class read the odata.metadata from the JsonElement to find out the real type of object contained inside the object 
 *   and the routes the parsing back to the correct default gson parser.
 */
public class SFCustomSafeEnumFlagsParser implements JsonDeserializer<SFSafeEnumFlags>, JsonSerializer<SFSafeEnumFlags>
{		
	private static final String TAG = "SFCustomSafeEnumParser";
	
	@Override
	public SFSafeEnumFlags deserialize(JsonElement jsonElement, Type typeOfObject,JsonDeserializationContext desContext) throws JsonParseException
	{
        Class enumClass = SafeEnumHelpers.getEnumClass(typeOfObject.toString());

        String value = jsonElement.getAsString();

        SFSafeEnumFlags safeEnum = new SFSafeEnumFlags();
        safeEnum.add(enumClass,value);

        return safeEnum;
	}

    @Override
    public JsonElement serialize(SFSafeEnumFlags sfSafeEnum, Type type, JsonSerializationContext jsonSerializationContext)
    {
        return new JsonPrimitive(sfSafeEnum.getOriginalString());
    }
}