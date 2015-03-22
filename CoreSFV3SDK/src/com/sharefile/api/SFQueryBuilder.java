package com.sharefile.api;

import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.entities.SFAccessControlsEntity;
import com.sharefile.api.entities.SFAccountsEntity;
import com.sharefile.api.entities.SFAsyncOperationsEntity;
import com.sharefile.api.entities.SFCapabilitiesEntity;
import com.sharefile.api.entities.SFConfigsEntity;
import com.sharefile.api.entities.SFDevicesEntity;
import com.sharefile.api.entities.SFFavoriteFoldersEntity;
import com.sharefile.api.entities.SFGroupsEntity;
import com.sharefile.api.entities.SFItemsEntity;
import com.sharefile.api.entities.SFMetadataEntity;
import com.sharefile.api.entities.SFSessionsEntity;
import com.sharefile.api.entities.SFSharesEntity;
import com.sharefile.api.entities.SFStorageCentersEntity;
import com.sharefile.api.entities.SFUsersEntity;
import com.sharefile.api.entities.SFZonesEntity;
import com.sharefile.api.enumerations.SFProvider;
import com.sharefile.api.extensions.SFCapabilitiesEntityEx;

import java.net.URI;
import java.net.URISyntaxException;

/** 
 * The entities have non-static get* functions to build the queries. The auto-generated SDK does not want to change this. 
 * lets simplify building the queries with the query builder class which holds static entity references so that the app does 
 * not haver to  create a new entity object every time it needs to build a query.
 */
public class SFQueryBuilder
{
	public static final SFAccessControlsEntity ACCESS_CONTROL = new SFAccessControlsEntity(null);
	public static final SFAccountsEntity ACCOUNTS = new SFAccountsEntity(null);
	public static final SFAsyncOperationsEntity ASYNC_OPERATION = new SFAsyncOperationsEntity(null);
	public static final SFCapabilitiesEntityEx CAPABILITIES = new SFCapabilitiesEntityEx(null);
	public static final SFConfigsEntity CONFIG = new SFConfigsEntity(null);
	public static final SFFavoriteFoldersEntity FAVORITE_FOLDERS = new SFFavoriteFoldersEntity(null);
	public static final SFGroupsEntity GROUPS = new SFGroupsEntity(null);
	public static final SFItemsEntity ITEMS = new SFItemsEntity(null);
	public static final SFMetadataEntity METADATA = new SFMetadataEntity(null);
	public static final SFSessionsEntity SESSIONS = new SFSessionsEntity(null);
	public static final SFSharesEntity SHARES = new SFSharesEntity(null);
	public static final SFUsersEntity USERS = new SFUsersEntity(null);
	public static final SFStorageCentersEntity STORAGE_CENTER = new SFStorageCentersEntity(null);
	public static final SFZonesEntity ZONES = new SFZonesEntity(null);
    public static final SFDevicesEntity DEVICES = new SFDevicesEntity(null);
		
	private static final String FORMAT_GET_TOP_FOLDER = "https://%s.%s"+SFProvider.PROVIDER_TYPE_SF+"Items(%s)";
	private static final String FORMAT_GET_DEVICES = "https://%s.%s"+SFProvider.PROVIDER_TYPE_SF+"Devices(%s)";
	
	
	/**
	 *   We need to manually construct the v3 url for the TOP folder. This function provides the helper for the apps
	 *   to build that url.
	 */
	public static final URI getDefaultURL(final String subdomain,String hostname,final String folderID) throws URISyntaxException
    {
          URI uri;
          
          String urlSpec = String.format(FORMAT_GET_TOP_FOLDER, subdomain,SFSDK.getApiServer(hostname),folderID);
          
          uri = new URI(urlSpec);
                      
          return uri;
    }
	
	public static final URI getDeviceURL(final String subdomain, String hostname, final String deviceID) throws URISyntaxException
	{
		URI uri;
        
        String urlSpec = String.format(FORMAT_GET_DEVICES, subdomain,SFSDK.getApiServer(hostname),deviceID);
        
        uri = new URI(urlSpec);
                    
        return uri;
	}
}