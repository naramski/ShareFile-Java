package com.sharefile.api.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.sharefile.api.models.SFODataObject;

public class SFDefaultGsonParser 
{
	static <T> SFODataObject parse(T typeO,JsonElement jsonElement)	
	{
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		return (SFODataObject) gson.fromJson(jsonElement, (Class<T>) typeO);		
	}		
}