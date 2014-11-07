package com.sharefile.api.gson.auto;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sharefile.api.SFSDKDefaultAccessScope;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.enumerations.SFSafeEnum;
import com.sharefile.api.models.SFGroup;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFShare;
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

    private static void addIfNonEmpty(JsonObject jsonObject , String key, Boolean value)
    {
        if( !Utils.isEmpty(key) && value !=null)
        {
            jsonObject.addProperty(key, value);
        }
    }

    private static void addIfNonEmpty(JsonObject jsonObject , String key, SFSafeEnum value)
    {
        if( !Utils.isEmpty(key) && value !=null)
        {
            jsonObject.addProperty(key, value.getOriginalString());
        }
    }

    private static void addIfNonEmpty(JsonObject jsonObject , String key, Integer value)
    {
        if( !Utils.isEmpty(key) && value !=null)
        {
            jsonObject.addProperty(key, value);
        }
    }

    private static void addIfNonEmpty(JsonObject jsonObject , String key, JsonArray value)
    {
        if( !Utils.isEmpty(key) && value !=null)
        {
            jsonObject.add(key, value);
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

    @SFSDKDefaultAccessScope static JsonArray serialize(ArrayList<SFItem> sfItems)
    {
        if(sfItems == null)
        {
            return null;
        }

        JsonArray jsonObject = new JsonArray();

        for(SFItem item: sfItems)
        {
            jsonObject.add(serialize(item));
        }

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

    @SFSDKDefaultAccessScope static JsonObject serialize(SFShare sfshare)
    {
        if(sfshare == null)
        {
            return null;
        }

        JsonObject jsonObject = new JsonObject();

        addIfNonEmpty(jsonObject, SFKeywords.Id, sfshare.getId());
        addIfNonEmpty(jsonObject, SFKeywords.URL,sfshare.geturl());
        addIfNonEmpty(jsonObject, "SendMethod",sfshare.getSendMethod());
        addIfNonEmpty(jsonObject, "Uri",sfshare.getUri());
        addIfNonEmpty(jsonObject, "Items",serialize(sfshare.getItems()));
        addIfNonEmpty(jsonObject, "RequireLogin",sfshare.getRequireLogin());
        addIfNonEmpty(jsonObject, "RequireUserInfo",sfshare.getRequireUserInfo());
        addIfNonEmpty(jsonObject, "MaxDownloads",sfshare.getMaxDownloads());
        addIfNonEmpty(jsonObject, "ShareType",sfshare.getShareType());

        return jsonObject;
    }
}