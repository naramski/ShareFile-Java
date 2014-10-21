package com.sharefile.api.gson.auto;

import java.lang.reflect.Type;
import java.net.URI;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sharefile.api.SFSDKDefaultAccessScope;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.models.SFGroup;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFUser;
import com.sharefile.api.utils.Utils;
import com.sharefile.java.log.SLog;

/**
 *  Class to group the customized serialization of SFObjects to json.
 */
public class SFCustomSerializer 
{
	private static void addIfNonEmpty(JsonObject jsonObject , String key, String value)
	{
		if( !Utils.isEmpty(key) && !Utils.isEmpty(value))
		{
			jsonObject.addProperty(key, value);
		}
	}

    private static void addIfNonEmpty(JsonObject jsonObject , String key, URI value)
    {
        if( !Utils.isEmpty(key) && value!=null && !Utils.isEmpty(value.toString()))
        {
            jsonObject.addProperty(key, value.toString());
        }
    }

    private static void add(JsonObject jsonObject , String key, String value, String defaultValue)
    {
        if( !Utils.isEmpty(key))
        {
            if(!Utils.isEmpty(value))
            {
                jsonObject.addProperty(key, value);
            }
            else
            {
                jsonObject.addProperty(key, defaultValue);
            }
        }
    }

	@SFSDKDefaultAccessScope static JsonElement serialize(SFODataObject sfODataObject, Type typeOfObject)
	{
		/*
		   dont call SFDefaultGsonParser.serialize(typeOfObject, sfODataObject);
		   or it leads to StackOverflow due to infinite recursive calls back here.
		*/
        throw new RuntimeException("Implement the custom serializer for: " + sfODataObject.getClass().getCanonicalName());
	}
	
	@SFSDKDefaultAccessScope static JsonObject serialize(SFItem sfItem)
	{		
		if(sfItem == null)
		{			
			return null;
		}
								
		JsonObject jsonObject = new JsonObject();
		
		addIfNonEmpty(jsonObject, SFKeywords.Id, sfItem.getId());
        add(jsonObject, SFKeywords.Description, sfItem.getDescription(), "");
        addIfNonEmpty(jsonObject,SFKeywords.URL,sfItem.geturl());
						
		return jsonObject;
	}
	
	@SFSDKDefaultAccessScope static JsonObject serialize(SFUser sfuser)
	{				
		if(sfuser == null)
		{						
			return null;
		}
				
		JsonObject jsonObject = new JsonObject();
				
		addIfNonEmpty(jsonObject, SFKeywords.Id, sfuser.getId());
		addIfNonEmpty(jsonObject, "Email", sfuser.getEmail());			
		addIfNonEmpty(jsonObject, "FirstName", sfuser.getFirstName());				
		addIfNonEmpty(jsonObject, "LastName", sfuser.getLastName());				
		addIfNonEmpty(jsonObject, "Company", sfuser.getCompany());
								
		return jsonObject;
	}
	
	@SFSDKDefaultAccessScope static JsonObject serialize(SFGroup sfgroup)
	{				
		if(sfgroup == null)
		{						
			return null;
		}
				
		JsonObject jsonObject = new JsonObject();
		
		addIfNonEmpty(jsonObject, SFKeywords.Id, sfgroup.getId());
		addIfNonEmpty(jsonObject, "Email", sfgroup.getEmail());				
		
		return jsonObject;
	}
}