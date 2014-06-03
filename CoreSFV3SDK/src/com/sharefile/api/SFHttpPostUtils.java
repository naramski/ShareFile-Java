package com.sharefile.api;

import com.google.gson.JsonObject;
import com.sharefile.api.constants.SFKeywords;

/**
 *   Contains the functiosn to create the POST BODY part for different http requests
 */
public class SFHttpPostUtils 
{
	/**
	* Create Folder
    * {
    * "Name":"Folder Name",
    * "Description":"Description",
    * "Zone":{ "Id":"z014766e-8e96-4615-86aa-57132a69843c" }
    * }
    */
	public static String getBodyCreateFolder(String name, String description,String zoneId)
	{
		JsonObject jsonObject = new JsonObject();
		
		jsonObject.addProperty(SFKeywords.Name, name);
		
		if(description==null)
		{
			description = "";
		}
		
		jsonObject.addProperty(SFKeywords.Description, description);
		
		if(zoneId!=null)
		{
			JsonObject zoneDetails = getJsonObject(SFKeywords.Id, zoneId);
			jsonObject.add(SFKeywords.Zone, zoneDetails);
		}
		
		return jsonObject.toString();
	}
	
	
	public static JsonObject getJsonObject(String key , String Value)
	{
		JsonObject jsonObject = new JsonObject();
		
		jsonObject.addProperty(key, Value);
				
		return jsonObject;
	}
		
}
