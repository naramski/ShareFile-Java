package com.sharefile.api.gson;

import com.google.gson.JsonObject;
import com.sharefile.api.models.SFAccountUser;
import com.sharefile.api.models.SFFile;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFLink;
import com.sharefile.api.models.SFNote;
import com.sharefile.api.models.SFSession;

public class SFClassSpecificGsonParser 
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
		/*
		"Company": "Citrix Systems, Inc.",
		  "TotalSharedFiles": 0,
		  "Contacted": 0,
		  "FullName": "Nilesh Pawar",
		  "ReferredBy": "3b0e16f6-3194-4428-a23b-3ea2475e77af",
		  "FirstName": "Nilesh",
		  "LastName": "Pawar",
		  "DateCreated": "2011-10-13T16:37:21.297Z",
		  "FullNameShort": "N. Pawar",
		  "IsConfirmed": true,
		  "Roles": [
		    "Employee",
		    "CanChangePassword",
		    "CanManageMySettings",
		    "CanUseFileBox",
		    "CanManageUsers",
		    "AdminCreateSharedGroups"
		  ],
		  "Email": "Nilesh.Pawar@citrix.com",
		  "Username": "Nilesh.Pawar@citrix.com",
		  "odata.metadata": "https://citrix.sf-api.com/sf/v3/$metadata#Users/ShareFile.Api.Models.AccountUser@Element",
		  "Id": "6e653483-0234-4e34-ace5-107ff624bbda",
		  "url": "https://citrix.sf-api.com/sf/v3/Users(6e653483-0234-4e34-ace5-107ff624bbda)"
		*/
		
		object.setCompany(SFGsonHelper.getString(jsonObject, "Company", null));
		object.setTotalSharedFiles(SFGsonHelper.getInt(jsonObject, "TotalSharedFiles", 0));
		object.setTotalSharedFiles(SFGsonHelper.getInt(jsonObject, "FullName", 0));
			  
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
