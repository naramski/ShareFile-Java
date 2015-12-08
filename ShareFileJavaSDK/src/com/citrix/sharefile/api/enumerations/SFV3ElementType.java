package com.citrix.sharefile.api.enumerations;

import com.citrix.sharefile.api.constants.SFKeywords;
import com.citrix.sharefile.api.exceptions.SFInvalidTypeException;
import com.citrix.sharefile.api.gson.auto.SFDefaultGsonParser;
import com.citrix.sharefile.api.log.Logger;
import com.citrix.sharefile.api.models.*;

import java.util.Iterator;
import java.util.Map;

public enum SFV3ElementType
{							
	File("Models.File@Element",SFFile.class),
	Folder("Models.Folder@Element",SFFolder.class),
	Item("Models.Item@Element",SFItem.class),
	ItemInfo("Models.ItemInfo",SFItemInfo.class),
	Link("Models.Link@Element",SFLink.class),
	Note("Models.Note@Element",SFNote.class),
	StorageCenter("Models.StorageCenter@Element",SFStorageCenter.class),
	SymbolicLink("Models.SymbolicLink@Element",SFSymbolicLink.class);

	private static final String prefix = "Models.";
	private static final String suffix = "@Element";
			
	private static final String TAG = SFKeywords.TAG + "-SFV3ElementType";
	private final String mToString;
	private final Class<?> mOriginalClass;//This is the one originally intended by the SDK
	private Class<?> mOverrideClass;// This is the one that can be overriden by the consumer app.
	
	private SFV3ElementType(String toStr,Class<?> clazz)
	{
		mToString = toStr;
		mOriginalClass = clazz;
		mOverrideClass = mOriginalClass;
	}
	
	@Override
	public String toString() 
	{		
		return mToString;
	}

    /**
        returns the ShareFile type string as required by the odata.type
     */
    public String type()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("ShareFile.Api.");

        int index = mToString.indexOf("@");

        if(index>0)
        {
            sb.append(mToString.substring(0, index));
        }
        else
        {
            sb.append(mToString);
        }

        return sb.toString();
    }
	
	public Class<?> getV3Class()
	{
		return mOverrideClass;
	}
	
	/** 
	 *  We are allowing consumers of the SDK to register their own deriived classes from the base models 
	 *  we have inside the SDK. This allows for cases where the consumer wants to add addtional flags and functions
	 *  to the model and yet have orginal parsed objects of his liking. Example SFFile does not provide the isSynced
	 *  flag. The consumer app can extend like : 
	 *  	<p>SFFileEx extends SFFile 
	 *  	<br>{ 
	 *  	<br>	boolean mIsSync
	 *  	<br>}
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SFInvalidTypeException 
	 */
	public static void registerSubClass(SFV3ElementType elementType, Class<?> newClass) throws InstantiationException, IllegalAccessException, SFInvalidTypeException
	{
		if(newClass == null)
		{
			throw new SFInvalidTypeException(" NULL does not extend " + elementType.mOriginalClass.toString());
		}
				 						
		//test if the new class is a real extension of the type being replaced.
		if(!elementType.mOriginalClass.isInstance(newClass.newInstance()))
		{
			String msg = newClass.toString() + " does not extend " + elementType.mOriginalClass.toString();
			
			Logger.d(TAG, msg);
			
			throw new SFInvalidTypeException(msg);
		}
		
		Logger.d(TAG, "Successfully registered : " + newClass.toString() + " to replace " + elementType.mOriginalClass.toString());
		
		elementType.mOverrideClass = newClass;

		registerSubClass(elementType.mToString.replace(prefix,"").replace(suffix,""),newClass);
	}

	public static void registerSubClass(String originalClassName, Class<?> newClass) throws InstantiationException, IllegalAccessException, SFInvalidTypeException
	{
		if(newClass == null || originalClassName == null)
		{
			throw new SFInvalidTypeException(" NULL classes not allowed ");
		}

		Class originalClass = SFEntityTypeMap.getEntityTypeMap().get(originalClassName);

		if(originalClass == null)
		{
			throw new SFInvalidTypeException("Given Class does not exist");
		}

		//test if the new class is a real extension of the type being replaced.
		if(!originalClass.isInstance(newClass.newInstance()))
		{
			String msg = newClass.toString() + " does not extend " + originalClass.toString();

			Logger.d(TAG, msg);

			throw new SFInvalidTypeException(msg);
		}

		SFEntityTypeMap.getEntityTypeMap().put(originalClassName,newClass);
		SFDefaultGsonParser.routeSpecialClasses(originalClassName,newClass);

		Logger.d(TAG, "Successfully registered : " + newClass.toString() + " to replace " + originalClass.toString());
	}

	
	public static boolean isFolderType(SFODataObject object)
	{
		boolean ret = false;
		
		if(object!=null)
		{
			if(object instanceof SFFolder)
			{
				ret = true;
			}
		}
		
		return ret;
	}
	
	public static boolean isFileType(SFODataObject object)
	{
		boolean ret = false;
		
		if(object!=null)
		{
			if(object instanceof SFFile)
			{
				ret = true;
			}
		}
		
		return ret;
	}
	
	public static boolean isNoteType(SFODataObject object)
	{
		boolean ret = false;
		
		if(object!=null)
		{
			if(object instanceof SFNote)
			{
				ret = true;
			}
		}
		
		return ret;
	}
	
	public static boolean isLinkType(SFODataObject object)
	{
		boolean ret = false;
		
		if(object!=null)
		{
			if(object instanceof SFLink)
			{
				ret = true;
			}
		}
		
		return ret;
	}
	
	public static boolean isSymbolicLinkType(SFODataObject object)
	{
		boolean ret = false;
		
		if(object!=null)
		{
			if(object instanceof SFSymbolicLink)
			{
				ret = true;
			}
		}
		
		return ret;
	}

}