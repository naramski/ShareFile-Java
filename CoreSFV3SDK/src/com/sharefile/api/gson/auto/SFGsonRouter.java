package com.sharefile.api.gson.auto;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sharefile.api.gson.SFGsonHelper;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFUser;
import com.sharefile.java.log.SLog;

/**
 *   This class read the odata.metadata from the JsonElement to find out the real type of object contained inside the object 
 *   and the routes the parsing back to the correct defuault gson parser.
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
		String str = null;
		
		if(sfODataObject instanceof SFItem) {
			str = "{\"Id\":\"" + sfODataObject.getId() + "\"}";
		}
		else if(sfODataObject instanceof SFUser) {
			SFUser sfuser = (SFUser) sfODataObject;
			str = "{";
			if(sfuser.getId()!=null && sfuser.getId().length()>0)
				str += "\"Id\":\"" + sfuser.getId() + "\",";
			if(sfuser.getEmail()!=null && sfuser.getEmail().length()>0)
				str += "\"Email\":\"" + sfuser.getEmail() + "\",";
			if(sfuser.getFirstName()!=null && sfuser.getFirstName().length()>0)
				str += "\"FirstName\":\"" + sfuser.getFirstName() + "\",";
			if(sfuser.getLastName()!=null && sfuser.getLastName().length()>0)
				str += "\"LastName\":\"" + sfuser.getLastName() + "\",";
			if(sfuser.getCompany()!=null && sfuser.getCompany().length()>0)
				str += "\"Company\":\"" + sfuser.getCompany() + "\",";
			if(str.length()>2)
				str = str.substring(0, str.length()-1);
			str += "}";
		}
		else {
			str = SFDefaultGsonParser.serialize(typeOfObject, sfODataObject);
		}
		
		if(str!=null)
		{
			JsonParser parser = new JsonParser();
			ret = parser.parse(str);
		}	
		
		/*
		SFV3ElementType elementType = SFModelFactory.getElementTypeClassName(typeOfObject.getClass().getName());
		
		switch(elementType)
		{
			case AccountUser:				
				str = SFDefaultGsonParser.serialize(SFAccountUser.class, sfODataObject);				
			break;
			
			case File:
				str = SFDefaultGsonParser.serialize(SFFile.class, sfODataObject);
			break;
			
			case Folder:
				str = SFDefaultGsonParser.serialize(SFFolder.class, sfODataObject);
			break;
			
			case Link:
				str = SFDefaultGsonParser.serialize(SFLink.class, sfODataObject);
			break;
			
			case Note:
				str = SFDefaultGsonParser.serialize(SFNote.class, sfODataObject);
			break;
			
			default:
				SFToDoReminderException.throwTODOException("Need to implement parser for : " + elementType.toString());
			break;	
		}*/
		
		return ret;
	}			
}