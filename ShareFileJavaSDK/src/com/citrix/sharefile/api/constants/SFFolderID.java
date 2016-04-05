package com.citrix.sharefile.api.constants;

/**
 *  Special folder IDs related to sharefile. We could have made this an enum but the usage becomes too clumsy at most places
 *  we will be using them since every time we need to compare it with a server returned id we would need to call toString on the enum.
 */
public final class SFFolderID 
{	
		public static final String ROOT = "root";
		public static final String TOP = "top";
		public static final String FILEBOX = "box";
		public static final String ALLSHARED = "allshared";
		public static final String FAVORITES = "favorites";
		public static final String CONNECTOR_SHAREPOINT = "c-sp";
		public static final String CONNECTOR_NETWORKSHARE = "c-cifs";
        public static final String SEARCH_RESULTS = "search_results";
		public static final String CONNECTOR_SHARE_CONNECT = "c-shareconnect";
		public static final String CONNECTOR_PCC = "personal_clound_connector";
		public static final String CONNECTOR_OFFICE365 = "office_365_connector";
		public static final String CONNECTOR_BOX = "c-Box";
		public static final String CONNECTOR_DROPBOX = "c-Dropbox";
		public static final String CONNECTOR_GOOGLE_DRIVE = "c-GoogleDrive";
		public static final String CONNECTOR_ONE_DRIVE = "c-OneDrive";
		public static final String CONNECTOR_SHAREPOINT_BUSINESS = "c-sp365";
		public static final String CONNECTOR_ONE_DRIVE_BUSINESS = "c-odb365";

}