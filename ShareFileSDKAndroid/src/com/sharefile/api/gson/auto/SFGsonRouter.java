package com.sharefile.api.gson.auto;

import java.lang.reflect.Type;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sharefile.api.SFModelFactory;
import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.exceptions.SFToDoReminderException;
import com.sharefile.api.gson.SFGsonHelper;
import com.sharefile.api.gson.manualparser.SFParse;
import com.sharefile.api.models.SFAccountUser;
import com.sharefile.api.models.SFFile;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFLink;
import com.sharefile.api.models.SFNote;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSymbolicLink;

/**
 *   This class read the odata.metadata from the JsonElement to find out the real type of object contained inside the object 
 *   and the routes the parsing back to the correct defuault gson parser.
 */
public class SFGsonRouter implements JsonDeserializer<SFODataObject>, JsonSerializer<SFODataObject>
{		
	private static final String TAG = "-SFGsonRouter";
	
	@Override
	public SFODataObject deserialize(JsonElement jsonElement, Type typeOfObject,JsonDeserializationContext desContext) throws JsonParseException 
	{		
		SFODataObject ret = null;
		
		try
		{
			if(jsonElement!=null)
			{
				
				SFLog.d2(TAG,"Route for %s" +  jsonElement.toString());
				
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				
				if(jsonObject!=null)
				{
					String metadata = SFGsonHelper.getString(jsonObject, SFKeywords.ODATA_METADATA, null);
					SFV3ElementType elementType = SFV3ElementType.getElementTypeFromMetaData(metadata);
										
					SFLog.d2(TAG, "GSON For : %s", metadata);
					
					switch(elementType)
					{
						case AccountUser:
							ret = SFDefaultGsonParser.parse(SFAccountUser.class, jsonElement);
						break;
						
						case File:
							ret = SFDefaultGsonParser.parse(SFFile.class, jsonElement);
						break;
						
						case Folder:
							ret = SFDefaultGsonParser.parse(SFFolder.class, jsonElement);
						break;
						
						/* SymbolicLink are folders that act as a routing link that pass-on from ShareFile to to CIFs/SP/etc */
						case SymbolicLink:
							ret = SFDefaultGsonParser.parse(SFSymbolicLink.class, jsonElement);
						break;
						
						case Link:
							ret = SFDefaultGsonParser.parse(SFLink.class, jsonElement);
						break;
						
						case Note:
							ret = SFDefaultGsonParser.parse(SFNote.class, jsonElement);
						break;
						
						case Item:
							ret = SFParse.parseSFItem(jsonObject);
						break;
						
						case CapabilityFeed:
							ret = SFParse.parseCapabilityFeed(jsonObject);
						break;	
						
						default:
							SFToDoReminderException.throwTODOException("Need to implement parser for : " + elementType.toString());
						break;	
					}
				}
				else
				{
					SFLog.d2(TAG,"JSON Object NULL");
				}
			}
			else
			{
				SFLog.d2(TAG,"JSON Element NULL");
			}
		}
		catch(Exception e)
		{									
			SFLog.d2(TAG,"Exception MSG = %s"  , Log.getStackTraceString(e));
		}
		
		if(ret ==null)
		{
			SFLog.d2(TAG,"Returning null  ");
		}
		else
		{
			if(ret instanceof SFFile)
			{
				SFLog.d2(TAG,"Returning NON null  %s" , ((SFFile)ret).getName());
			}
		}
		
		return ret;
	}

	@Override
	public JsonElement serialize(SFODataObject sfODataObject, Type typeOfObject,JsonSerializationContext serContext) 
	{	
		JsonElement ret = null;
		String str = null;
		
		str = SFDefaultGsonParser.serialize(typeOfObject, sfODataObject);
		
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