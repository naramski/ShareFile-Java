
// ------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//  
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
//     
//	   Copyright (c) 2015 Citrix ShareFile. All rights reserved.
// </auto-generated>
// ------------------------------------------------------------------------------

package com.citrix.sharefile.api.entities;

import com.citrix.sharefile.api.exceptions.InvalidOrMissingParameterException;
import com.citrix.sharefile.api.interfaces.ISFApiClient;
import com.citrix.sharefile.api.models.*;
import com.citrix.sharefile.api.SFApiQuery;
import com.citrix.sharefile.api.interfaces.ISFQuery;


import java.net.URI;

public class SFFolderTemplatesEntity extends SFODataEntityBase
{
	public SFFolderTemplatesEntity(ISFApiClient client) {
		super(client);
	}

	/**
	* Get Folder Templates for Current Account
	* Returns all folder templates for the current account.
	* @return Folder templates for current account
	*/
	public ISFQuery<SFODataFeed<SFFolderTemplate>> get()	{

		SFApiQuery<SFODataFeed<SFFolderTemplate>> sfApiQuery = new SFApiQuery<SFODataFeed<SFFolderTemplate>>(this.apiClient);
		sfApiQuery.setFrom("FolderTemplates");
		sfApiQuery.setHttpMethod("GET");
		return sfApiQuery;
	}

	public ISFQuery<SFFolderTemplate> get(URI url) throws InvalidOrMissingParameterException 	{
		if (url == null) {
			throw new InvalidOrMissingParameterException("url");
		}

		SFApiQuery<SFFolderTemplate> sfApiQuery = new SFApiQuery<SFFolderTemplate>(this.apiClient);
		sfApiQuery.setFrom("FolderTemplates");
		sfApiQuery.addIds(url);
		sfApiQuery.setHttpMethod("GET");
		return sfApiQuery;
	}

	/**
	* Create Folder Template
    * {
    * "Name": "Client Folder",
    * "Description": "For all apiClient folders created in 2014 or after"
    * "Items": [
    * {
    * "Name": "Folder 1",
    * "Items": [
    * {
    * "Name": "Subfolder1"
    * }
    * ]
    * },
    * {
    * "Name": "Folder 2"
    * }
    * ]
	* Creates a new folder template.
	* @return The created folder template
	*/
	public ISFQuery<SFFolderTemplate> create(SFFolderTemplate template) throws InvalidOrMissingParameterException 	{
		if (template == null) {
			throw new InvalidOrMissingParameterException("template");
		}

		SFApiQuery<SFFolderTemplate> sfApiQuery = new SFApiQuery<SFFolderTemplate>(this.apiClient);
		sfApiQuery.setFrom("FolderTemplates");
		sfApiQuery.setBody(template);
		sfApiQuery.setHttpMethod("POST");
		return sfApiQuery;
	}

	/**
	* Update Folder Template
    * {
    * "Name": "Client Folder",
    * "Description": "For all apiClient folders created in 2014 or after",
    * "Items": [
    * {
    * "Name": "A new folder",
    * "Items": [
    * {
    * "TemplateId": "ft-12345-abdef-abdef-12345-abdef",
    * "Name": "A new subfolder"
    * }
    * ]
    * },
    * {
    * "Id": "fti-xxxxx-xxxxx-xxxxx-xxxxx",
    * "Name": "An existing folder with a different name"
    * }
    * ]
    * }
	* Updates an existing folder template
	* @return The updated folder template
	*/
	public ISFQuery<SFFolderTemplate> update(SFFolderTemplate template) throws InvalidOrMissingParameterException 	{
		if (template == null) {
			throw new InvalidOrMissingParameterException("template");
		}

		SFApiQuery<SFFolderTemplate> sfApiQuery = new SFApiQuery<SFFolderTemplate>(this.apiClient);
		sfApiQuery.setFrom("FolderTemplates");
		sfApiQuery.setBody(template);
		sfApiQuery.setHttpMethod("PATCH");
		return sfApiQuery;
	}

	/**
	* Delete Folder Template
	* Removes a folder template
	* @param url 	 	
	*/
	public ISFQuery delete(URI url) throws InvalidOrMissingParameterException 	{
		if (url == null) {
			throw new InvalidOrMissingParameterException("url");
		}

		SFApiQuery sfApiQuery = new SFApiQuery(this.apiClient);
		sfApiQuery.setFrom("FolderTemplates");
		sfApiQuery.addIds(url);
		sfApiQuery.setHttpMethod("DELETE");
		return sfApiQuery;
	}

}
