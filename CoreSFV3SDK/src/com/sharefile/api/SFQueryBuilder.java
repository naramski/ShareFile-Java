package com.sharefile.api;

import com.sharefile.api.entities.*;

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
}