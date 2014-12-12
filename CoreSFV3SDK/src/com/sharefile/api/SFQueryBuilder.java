package com.sharefile.api;

import java.net.URI;
import java.net.URISyntaxException;

import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.entities.*;
import com.sharefile.api.entities.internal.SFStorageCentersEntityInternal;
import com.sharefile.api.entities.internal.SFZonesEntityInternal;
import com.sharefile.api.enumerations.SFProvider;

/** 
 * The entities have non-static get* functions to build the queries. The auto-generated SDK does not want to change this. 
 * lets simplify building the queries with the query builder class which holds static entity references so that the app does 
 * not haver to  create a new entity object every time it needs to build a query.
 */
public class SFQueryBuilder
{
	public static final SFAccessControlsEntity ACCESS_CONTROL = new SFAccessControlsEntity();
	public static final SFAccountsEntity ACCOUNTS = new SFAccountsEntity();
	public static final SFAsyncOperationsEntity ASYNC_OPERATION = new SFAsyncOperationsEntity();
	public static final SFCapabilitiesEntity CAPABILITIES = new SFCapabilitiesEntity();
	public static final SFConfigsEntity CONFIG = new SFConfigsEntity();
	public static final SFFavoriteFoldersEntity FAVORITE_FOLDERS = new SFFavoriteFoldersEntity();
	public static final SFGroupsEntity GROUPS = new SFGroupsEntity();
	public static final SFItemsEntity ITEMS = new SFItemsEntity();
	public static final SFMetadataEntity METADATA = new SFMetadataEntity();	
	public static final SFSessionsEntity SESSIONS = new SFSessionsEntity();
	public static final SFSharesEntity SHARES = new SFSharesEntity();
	public static final SFUsersEntity USERS = new SFUsersEntity();
	public static final SFStorageCentersEntity STORAGE_CENTER = new SFStorageCentersEntity();
	public static final SFZonesEntityInternal ZONES = new SFZonesEntityInternal();
    public static final SFDevicesEntity DEVICES = new SFDevicesEntity();
		
	private static final String FORMAT_GET_TOP_FOLDER = "https://%s.%s"+SFProvider.PROVIDER_TYPE_SF+"Items(%s)";
	private static final String FORMAT_GET_DEVICES = "https://%s.%s"+SFProvider.PROVIDER_TYPE_SF+"Devices(%s)";
	
	
	/**
	 *   We need to manually construct the v3 url for the TOP folder. This function provides the helper for the apps
	 *   to build that url.
	 */
	public static final URI getDefaultURL(final String subdomain,String hostname,final String folderID) throws URISyntaxException
    {
          URI uri = null;
          
          String urlSpec = String.format(FORMAT_GET_TOP_FOLDER, subdomain,SFSDK.getApiServer(hostname),folderID);
          
          uri = new URI(urlSpec);
                      
          return uri;
    }
	
	public static final URI getDeviceURL(final String subdomain, String hostname, final String deviceID) throws URISyntaxException
	{
		URI uri = null;
        
        String urlSpec = String.format(FORMAT_GET_DEVICES, subdomain,SFSDK.getApiServer(hostname),deviceID);
        
        uri = new URI(urlSpec);
                    
        return uri;
	}
}