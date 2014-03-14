package com.sharefile.api;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.reflect.TypeToken;
import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.models.SFAccountUser;
import com.sharefile.api.models.SFFile;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFLink;
import com.sharefile.api.models.SFNote;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSession;
import com.sharefile.api.models.SFUser;

public class SFModelFactory 
{
	private static final Map<SFV3ElementType, Class> mMapTypeClassPair;
	//Fill this on need basis.
	static 
	{
	        Map<SFV3ElementType, Class> aMap = new HashMap<SFV3ElementType, Class>();
	        
	        aMap.put(SFV3ElementType.File, SFFile.class);
	        aMap.put(SFV3ElementType.Folder, SFFolder.class);
	        aMap.put(SFV3ElementType.Link, SFLink.class);
	        aMap.put(SFV3ElementType.Note, SFNote.class);
	        aMap.put(SFV3ElementType.Session, SFSession.class);
	        aMap.put(SFV3ElementType.AccountUser, SFUser.class);	        
	        
	        mMapTypeClassPair = Collections.unmodifiableMap(aMap);
	}
			
	public static final SFODataObject createNewObjectFromElementType(String metadata)
	{
		SFODataObject ret = null;
		
		Set<SFV3ElementType> keySet = mMapTypeClassPair.keySet();
		
		for(SFV3ElementType s:keySet)
		{
			if(compare(metadata, s.toString()))
			{
				Class clazz = mMapTypeClassPair.get(s);		
				
				try 
				{
					ret = (SFODataObject) clazz.newInstance();
				} 
				catch (Exception e)
				{					
					e.printStackTrace();
				}
				
				break;
			}
		}
		
		return ret;
	}
	
	public static final SFV3ElementType getElementTypeFromMetaData(String metadata)
	{
		SFV3ElementType ret = null;
		
		Set<SFV3ElementType> keySet = mMapTypeClassPair.keySet();
		
		for(SFV3ElementType s:keySet)
		{
			if(compare(metadata, s.toString()))
			{
				ret = s;
				
				break;
			}
		}
		
		return ret;
	}
	
	
	public static final SFODataObject createNewObjectFromClassName(String className)
	{
		SFODataObject ret = null;
		
		Set<SFV3ElementType> keySet = mMapTypeClassPair.keySet();
		
		for(SFV3ElementType s:keySet)
		{
			
			Class clazz = mMapTypeClassPair.get(s);
			
			if(clazz.getName().equalsIgnoreCase(className))
			{
				try 
				{
					ret = (SFODataObject) clazz.newInstance();
				} 
				catch (Exception e)
				{					
					e.printStackTrace();
				}
				
				break;
			}
		}
		
		return ret;
	}
	
	
	public static final Type getTypeTokenFromClassName(String className)
	{
		Type ret = null;
		
		Set<SFV3ElementType> keySet = mMapTypeClassPair.keySet();
		
		for(SFV3ElementType s:keySet)
		{
			
			Class clazz = mMapTypeClassPair.get(s);
			
			if(clazz.getName().equalsIgnoreCase(className))
			{
				
				try 
				{
					switch (s) 
					{								        
						case File:
							ret = new TypeToken<SFFile>(){}.getType();
						break;
	
						case Folder:
							ret = new TypeToken<SFFolder>(){}.getType();
						break;
						
						case Link:
							ret = new TypeToken<SFLink>(){}.getType();
						break;
						
						case Note:
							ret = new TypeToken<SFNote>(){}.getType();
						break;
						
						case Session:
							ret = new TypeToken<SFSession>(){}.getType();
						break;
						
						case AccountUser:
							ret = new TypeToken<SFAccountUser>(){}.getType();
						break;
						default:
							ret = null;
						break;
					}
				} 
				catch (Exception e)
				{					
					e.printStackTrace();
				}
				
				break;
			}
		}
		
		return ret;
	}
	
	public static boolean compare(String metadata,String element)
	{
		return metadata.contains(element);
	}
	
	public static boolean isFolder(String metadata)
	{
		return compare(metadata, SFV3ElementType.Folder.toString());
	}
	
	public static boolean isFile(String metadata)
	{
		return compare(metadata, SFV3ElementType.File.toString());
	}
	
	public static boolean isLink(String metadata)
	{
		return compare(metadata, SFV3ElementType.Link.toString());
	}
	
	public static boolean isNote(String metadata)
	{
		return compare(metadata, SFV3ElementType.Note.toString());
	}	
	
	public static boolean isAccountUser(String metadata)
	{
		return compare(metadata, SFV3ElementType.AccountUser.toString());
	}
	
	public static boolean isSession(String metadata)
	{
		return compare(metadata, SFV3ElementType.Session.toString());
	}

}
