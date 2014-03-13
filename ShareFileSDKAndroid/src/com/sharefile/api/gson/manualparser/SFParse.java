package com.sharefile.api.gson.manualparser;

import com.google.gson.JsonObject;
import com.sharefile.api.models.SFAccountUser;
import com.sharefile.api.models.SFFile;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFLink;
import com.sharefile.api.models.SFNote;
import com.sharefile.api.models.SFSession;

public class SFParse 
{
		
	static SFSession parse(SFSession object,JsonObject jsonObject)	
	{
		return object;
	}
	
	static SFFile parse(SFFile object,JsonObject jsonObject)	
	{
		return object;
	}
	
	static SFFolder parse(SFFolder object,JsonObject jsonObject)	
	{
		return object;
	}
	
	static SFNote parse(SFNote object,JsonObject jsonObject)	
	{
		return object;
	}
		
	static SFLink parse(SFLink object,JsonObject jsonObject)	
	{
		return object;
	}
	
	static SFAccountUser parse(SFAccountUser object,JsonObject jsonObject)	
	{							  
		return object;
	}
	
	/**
	 * Add all new parse functions in the similar pattern
	 * <p> Note they should have package scope. Can't use Java generics coz of the problem with Java generics and erasures
	 * 
	    <T> parse(<T> object,JsonObject jsonObject)	
		{
			return object;
		} 		
	*/
}
