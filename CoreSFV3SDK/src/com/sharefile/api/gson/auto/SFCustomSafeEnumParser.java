package com.sharefile.api.gson.auto;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sharefile.api.enumerations.SFSafeEnum;
import com.sharefile.api.utils.SafeEnumHelpers;
import com.sharefile.api.utils.Utils;

import java.lang.reflect.Type;

/**
 *   This class read the odata.metadata from the JsonElement to find out the real type of object contained inside the object 
 *   and the routes the parsing back to the correct default gson parser.
 */
public class SFCustomSafeEnumParser implements JsonDeserializer<SFSafeEnum>, JsonSerializer<SFSafeEnum>
{		
	private static final String TAG = "SFCustomSafeEnumParser";
	
	@Override
	public SFSafeEnum deserialize(JsonElement jsonElement, Type typeOfObject,JsonDeserializationContext desContext) throws JsonParseException
	{
        SFSafeEnum safeEnum = new SFSafeEnum();

        Class enumClass = SafeEnumHelpers.getEnumClass(typeOfObject.toString(),false);

        String value = jsonElement.getAsString();

        Enum enuM = SafeEnumHelpers.getEnumFromString(enumClass, value);

        safeEnum.setValue(value, enuM);

        return safeEnum;
	}

    @Override
    public JsonElement serialize(SFSafeEnum sfSafeEnum, Type type, JsonSerializationContext jsonSerializationContext)
    {
        return new JsonPrimitive(sfSafeEnum.getOriginalString());
    }
}