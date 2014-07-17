package com.sharefile.api.constants;

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
}