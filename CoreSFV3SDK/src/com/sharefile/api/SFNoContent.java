package com.sharefile.api;

import com.sharefile.api.models.SFODataObject;

/**
 *  some of the V3 Apis return NO CONTENT . We call successApi on this and return this special object
 */
public class SFNoContent extends SFODataObject 
{
	public SFNoContent()
	{
		setId("NO-CONTENT");
	}
}
