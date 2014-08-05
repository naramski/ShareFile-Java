package com.sharefile.api.gson.auto;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sharefile.api.SFSDKDefaultAccessScope;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.models.SFGroup;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFUser;
import com.sharefile.java.log.SLog;

/**
 *  Class to group the customized serialization of SFObjects to json.
 */
public class SFCustomSerializer 
{
	@SFSDKDefaultAccessScope static JsonElement serialize(SFODataObject sfODataObject, Type typeOfObject)
	{
		String str = SFDefaultGsonParser.serialize(typeOfObject, sfODataObject);
		
		if(str==null)
		{
			return null;
		}
		
		JsonParser parser = new JsonParser();
		return parser.parse(str);
	}
	
	@SFSDKDefaultAccessScope static JsonObject serialize(SFItem sfItem)
	{		
		if(sfItem == null)
		{			
			return null;
		}
								
		JsonObject jsonObject = new JsonObject();
		
		String id = sfItem.getId();
		if(id!=null && id.length()>0)
		{
			jsonObject.addProperty(SFKeywords.Id, id);
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
		
		String id = sfuser.getId();
		if(id!=null && id.length()>0)
		{
			jsonObject.addProperty(SFKeywords.Id, id);
		}
		
		jsonObject.addProperty("Email", sfuser.getEmail());
		jsonObject.addProperty("FirstName", sfuser.getFirstName());
		jsonObject.addProperty("LastName", sfuser.getLastName());
		jsonObject.addProperty("Company", sfuser.getCompany());
						
		return jsonObject;
	}
	
	@SFSDKDefaultAccessScope static JsonObject serialize(SFGroup sfgroup)
	{				
		if(sfgroup == null)
		{						
			return null;
		}
				
		JsonObject jsonObject = new JsonObject();
		
		String id = sfgroup.getId();
		if(id!=null && id.length()>0)
		{
			jsonObject.addProperty(SFKeywords.Id, id);
		}
						
		jsonObject.addProperty("Email", sfgroup.getEmail());				
		
		return jsonObject;
	}
}