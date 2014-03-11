package com.sharefile.api.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonSyntaxException;
import com.sharefile.api.SFModelFactory;
import com.sharefile.api.exceptions.SFJsonException;
import com.sharefile.api.models.SFODataObject;

public class SFGsonParser 
{	
	public static void parseFromJson(SFODataObject object,String jsonString) throws SFJsonException
	{
		try
        {        			
			Type t = object.getClass();
        	SFGsonHelper.fromJson(jsonString, object.getClass(), SFModelFactory.getTypeTokenFromClassName(object.getClass().getName()), new SFCommonDeSerializer());
        }
        catch(JsonSyntaxException ex)
        {
        	throw new SFJsonException(ex);
        }
	}
	
	public static void parseFromJson(Class clazz,String jsonString) throws SFJsonException
	{
		try
        {        			
			//Type t = object.getClass();
        	SFGsonHelper.fromJson(jsonString, clazz, SFModelFactory.getTypeTokenFromClassName(clazz.getName()), new SFCommonDeSerializer());
        }
        catch(JsonSyntaxException ex)
        {
        	throw new SFJsonException(ex);
        }
	}
}