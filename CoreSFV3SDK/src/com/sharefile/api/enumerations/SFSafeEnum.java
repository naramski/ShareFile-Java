package com.sharefile.api.enumerations;

import com.google.gson.annotations.SerializedName;

public class SFSafeEnum<T extends Enum> 
{
	private Enum mEnum;	
	
	@SerializedName("value")
	private String originalString;
	
	public void setValue(String v, Enum e)
	{
		originalString = v;
		mEnum = e;
	}
	
	public SFSafeEnum(Enum e)
	{
		setValue(e.toString(), e);
	}
	
	public SFSafeEnum() 
	{
		
	}

	public String getOriginalString()
	{
		return originalString;
	}	
	
	public Enum get()
	{
		return mEnum;
	}
	
	@Override
	public String toString() 
	{		
		return originalString;
	}

    public boolean equals(Enum target)
    {
        return mEnum == target;
    }
}