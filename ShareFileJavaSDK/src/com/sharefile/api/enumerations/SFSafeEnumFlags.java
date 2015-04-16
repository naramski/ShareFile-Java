package com.sharefile.api.enumerations;

import com.google.gson.annotations.SerializedName;
import com.sharefile.api.utils.SafeEnumHelpers;

import java.util.HashSet;
import java.util.Set;

public class SFSafeEnumFlags<T extends Enum>
{
    private static final String FLAG_SEPARATOR = ",";

	private Set<T> mEnum = new HashSet<T>();

	@SerializedName("value")
	private String originalString = "";

    /**
        Removes all enum flags.
     */
    public void clear()
    {
        originalString = "";
        mEnum.clear();
    }

    private void appendToOriginal(String v)
    {
        if(v == null)
        {
            return;
        }

        if(originalString.length() > 0 && v.length() > 0)
        {
            originalString = originalString + FLAG_SEPARATOR;
        }

        originalString = originalString + v;
    }

	public void add(String v, T e)
	{
        appendToOriginal(v);

		mEnum.add(e);
	}

    private String buildString(Set<T> enums)
    {
        StringBuilder sb = new StringBuilder();

        boolean appendComma = false;

        for(T obj : enums)
        {
            if(obj == null)
            {
                continue;
            }

            if(appendComma)
            {
                sb.append(FLAG_SEPARATOR);
            }
            else
            {
                appendComma = true;
            }

            sb.append(obj.toString());
        }

        return sb.toString();
    }

    /**
        This will add the given set of enums to existing Set and also modify
        the original string accordingly
     */
    public void add(Set<T> enums)
    {
        appendToOriginal(buildString(enums));

        mEnum.addAll(enums);
    }

    private Set<T> buildSet(Class enumClass,String newFlags)
    {
        Set<T> newSet = new HashSet<T>();

        String[] parts = newFlags.split(FLAG_SEPARATOR);

        for(String str: parts)
        {
            Enum enuM = SafeEnumHelpers.getEnumFromString(enumClass, str.trim());

            if(enuM !=null)
            {
                newSet.add((T) enuM);
            }
        }

        return newSet;
    }

    public void add(Class enumClass,String newFlags)
    {
        appendToOriginal(newFlags);
        mEnum.addAll(buildSet(enumClass,newFlags));
    }


    public void remove(T e)
    {
        //TODO: remove the part from the original string too
        mEnum.remove(e);
    }

	public SFSafeEnumFlags(T e)
	{
		add(e.toString(), e);
	}

    public SFSafeEnumFlags(String original, Set<T> enums)
    {
        originalString =original;
        mEnum.addAll(enums);
    }

	public SFSafeEnumFlags()
	{
		
	}

	public String getOriginalString()
	{
		return originalString;
	}	
	
	public Set get()
	{
		return mEnum;
	}
	
	@Override
	public String toString() 
	{		
		return originalString;
	}

    public boolean contains(T target)
    {
        return mEnum.contains(target);
    }
}