package com.sharefile.api.enumerations;

import java.util.Set;

import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSymbolicLink;

public enum SFV3ElementType 
{
	/*
	public static final String AccessControl = "AccessControl@Element";
	public static final String AccessControlDomains = "AccessControlDomains@Element";
	public static final String Account = "Account@Element";
	public static final String AccountPreferences = "AccountPreferences@Element";
	public static final String AccountUser = "AccountUser@Element";
	public static final String AdvancedSearchResults = "AdvancedSearchResults@Element";
	public static final String AsyncOperation = "AsyncOperation@Element";
	public static final String Capability = "#Capabilities";
	public static final String Contact = "Contact@Element";
	public static final String Device = "Device@Element";
	public static final String DeviceLogEntry = "DeviceLogEntry@Element";
	public static final String DeviceStatus = "DeviceStatus@Element";
	public static final String DeviceUser = "DeviceUser@Element";
	public static final String DeviceUserWipe = "DeviceUserWipe@Element";
	public static final String DeviceWipeReport = "DeviceWipeReport@Element";
	public static final String DownloadSpecification = "DownloadSpecification@Element";
	public static final String FavoriteFolder = "FavoriteFolder@Element";
	public static final String File = "File@Element";
	public static final String FindSubdomainParams = "FindSubdomainParams@Element";
	public static final String FindSubdomainResult = "FindSubdomainResult@Element";
	public static final String Folder = "Folder@Element";
	public static final String GenericConfig = "GenericConfig@Element";
	public static final String Group = "Group@Element";
	public static final String Item = "Item@Element";
	public static final String ItemInfo = "ItemInfo@Element";
	public static final String ItemProtocolLink = "ItemProtocolLink@Element";
	public static final String Link = "Link@Element";
	public static final String Metadata = "Metadata@Element";
	public static final String MobileSecuritySettings = "MobileSecuritySettings@Element";
	public static final String Note = "Note@Element";
	public static final String Notification = "Notification@Element";
	public static final String ODataFeed = "ODataFeed@Element";
	public static final String ODataObject = "ODataObject@Element";
	public static final String PlanFeatures = "PlanFeatures@Element";
	public static final String Principal = "Principal@Element";
	public static final String ProductDefaults = "ProductDefaults@Element";
	public static final String Query = "Query@Element";
	public static final String QueryPaging = "QueryPaging@Element";
	public static final String QuerySorting = "QuerySorting@Element";
	public static final String RequireSubdomainResult = "RequireSubdomainResult@Element";
	public static final String RequireWebPopResult = "RequireWebPopResult@Element";
	public static final String SearchQuery = "SearchQuery@Element";
	public static final String SearchResult = "SearchResult@Element";
	public static final String SearchResults = "SearchResults@Element";
	public static final String Session = "Session@Element";
	public static final String Share = "Share@Element";
	public static final String ShareAlias = "ShareAlias@Element";
	public static final String ShareRequestParams = "ShareRequestParams@Element";
	public static final String ShareSendParams = "ShareSendParams@Element";
	public static final String SimpleQuery = "SimpleQuery@Element";
	public static final String SimpleSearchQuery = "SimpleSearchQuery@Element";
	public static final String SSOAccountProvider = "SSOAccountProvider@Element";
	public static final String SSOInfo = "SSOInfo@Element";
	public static final String SSOInfoEntry = "SSOInfoEntry@Element";
	public static final String StorageCenter = "StorageCenter@Element";
	public static final String SymbolicLink = "SymbolicLink@Element";
	public static final String UploadSpecification = "UploadSpecification@Element";
	public static final String User = "User@Element";
	public static final String UserConfirmationSettings = "UserConfirmationSettings@Element";
	public static final String UserInfo = "UserInfo@Element";
	public static final String UserPreferences = "UserPreferences@Element";
	public static final String UserSecurity = "UserSecurity@Element";
	public static final String Zone = "Zone@Element";
	public static final String OutlookInformation = "OutlookInformation@Element";
	public static final String OutlookInformationOptionBool = "OutlookInformationOptionBool@Element";
	public static final String OutlookInformationOptionInt = "OutlookInformationOptionInt@Element";
	public static final String OutlookInformationOptionString = "OutlookInformationOptionString@Element";
	*/
					
	AccessControlFeed{@Override public String toString() {return "$metadata#AccessControls";}},
	AccessControl{@Override public String toString() {return "Models.AccessControl@Element";}},	
	Account{@Override public String toString() {return "Models.Account@Element";}},
	AccountPreferences{@Override public String toString() {return "Models.AccountPreferences@Element";}},
	AccountUser{@Override public String toString() {return "Models.AccountUser@Element";}},
	AdvancedSearchResults{@Override public String toString() {return "Models.AdvancedSearchResults@Element";}},
	AsyncOperation{@Override public String toString() {return "Models.AsyncOperation@Element";}},
	Capability{@Override public String toString() {return "Models.Capabilities@Element";}},
	CapabilityFeed{@Override public String toString() {return "$metadata#Capabilities";}},
	Contact{@Override public String toString() {return "Models.Contact@Element";}},
	Device{@Override public String toString() {return "Models.Device@Element";}},
	DeviceLogEntry{@Override public String toString() {return "Models.DeviceLogEntry@Element";}},
	DeviceStatus{@Override public String toString() {return "Models.DeviceStatus@Element";}},
	DeviceUser{@Override public String toString() {return "Models.DeviceUser@Element";}},
	DeviceUserWipe{@Override public String toString() {return "Models.DeviceUserWipe@Element";}},
	DeviceWipeReport{@Override public String toString() {return "Models.DeviceWipeReport@Element";}},
	DownloadSpecification{@Override public String toString() {return "Models.DownloadSpecification@Element";}},
	FavoriteFolder{@Override public String toString() {return "Models.FavoriteFolder@Element";}},
	File{@Override public String toString() {return "Models.File@Element";}},
	FindSubdomainParams{@Override public String toString() {return "Models.FindSubdomainParams@Element";}},
	FindSubdomainResult{@Override public String toString() {return "Models.FindSubdomainResult@Element";}},
	Folder{@Override public String toString() {return "Models.Folder@Element";}},
	GenericConfig{@Override public String toString() {return "Models.GenericConfig@Element";}},
	Group{@Override public String toString() {return "Models.Group@Element";}},
	Item{@Override public String toString() {return "Models.Item@Element";}},
	ItemInfo{@Override public String toString() {return "Models.ItemInfo@Element";}},
	ItemProtocolLink{@Override public String toString() {return "Models.ItemProtocolLink@Element";}},
	Link{@Override public String toString() {return "Models.Link@Element";}},
	Metadata{@Override public String toString() {return "Models.Metadata@Element";}},
	MobileSecuritySettings{@Override public String toString() {return "Models.MobileSecuritySettings@Element";}},
	Note{@Override public String toString() {return "Models.Note@Element";}},
	Notification{@Override public String toString() {return "Models.Notification@Element";}},
	ODataFeed{@Override public String toString() {return "Models.ODataFeed@Element";}},
	ODataObject{@Override public String toString() {return "Models.ODataObject@Element";}},
	PlanFeatures{@Override public String toString() {return "Models.PlanFeatures@Element";}},
	Principal{@Override public String toString() {return "Models.Principal@Element";}},
	ProductDefaults{@Override public String toString() {return "Models.ProductDefaults@Element";}},
	Query{@Override public String toString() {return "Models.Query@Element";}},
	QueryPaging{@Override public String toString() {return "Models.QueryPaging@Element";}},
	QuerySorting{@Override public String toString() {return "Models.QuerySorting@Element";}},
	RequireSubdomainResult{@Override public String toString() {return "Models.RequireSubdomainResult@Element";}},
	RequireWebPopResult{@Override public String toString() {return "Models.RequireWebPopResult@Element";}},
	SearchQuery{@Override public String toString() {return "Models.SearchQuery@Element";}},
	SearchResult{@Override public String toString() {return "Models.SearchResult@Element";}},
	SearchResults{@Override public String toString() {return "Models.SearchResults@Element";}},
	Session{@Override public String toString() {return "Models.Session@Element";}},
	Share{@Override public String toString() {return "Models.Share@Element";}},
	ShareAlias{@Override public String toString() {return "Models.ShareAlias@Element";}},
	ShareRequestParams{@Override public String toString() {return "Models.ShareRequestParams@Element";}},
	ShareSendParams{@Override public String toString() {return "Models.ShareSendParams@Element";}},
	SimpleQuery{@Override public String toString() {return "Models.SimpleQuery@Element";}},
	SimpleSearchQuery{@Override public String toString() {return "Models.SimpleSearchQuery@Element";}},
	SSOAccountProvider{@Override public String toString() {return "Models.SSOAccountProvider@Element";}},
	SSOInfo{@Override public String toString() {return "Models.SSOInfo@Element";}},
	SSOInfoEntry{@Override public String toString() {return "Models.SSOInfoEntry@Element";}},
	StorageCenter{@Override public String toString() {return "Models.StorageCenter@Element";}},
	SymbolicLink{@Override public String toString() {return "Models.SymbolicLink@Element";}},
	UploadSpecification{@Override public String toString() {return "Models.UploadSpecification@Element";}},
	User{@Override public String toString() {return "Models.User@Element";}},
	UserConfirmationSettings{@Override public String toString() {return "Models.UserConfirmationSettings@Element";}},
	UserInfo{@Override public String toString() {return "Models.UserInfo@Element";}},
	UserPreferences{@Override public String toString() {return "Models.UserPreferences@Element";}},
	UserSecurity{@Override public String toString() {return "Models.UserSecurity@Element";}},
	Zone{@Override public String toString() {return "Models.Zone@Element";}},
	OutlookInformation{@Override public String toString() {return "Models.OutlookInformation@Element";}},
	OutlookInformationOptionBool{@Override public String toString() {return "Models.OutlookInformationOptionBool@Element";}},
	OutlookInformationOptionInt{@Override public String toString() {return "Models.OutlookInformationOptionInt@Element";}},
	OutlookInformationOptionString{@Override public String toString() {return "Models.OutlookInformationOptionString@Element";}};
	
	
	public static boolean isFolderType(SFODataObject object)
	{
		boolean ret = false;
		
		if(object!=null)
		{
			if(object instanceof SFFolder || object instanceof SFSymbolicLink)
			{
				ret = true;
			}
		}
		
		return ret;
	}
		
	public static final SFV3ElementType getElementTypeFromMetaData(String metadata)
	{
		SFV3ElementType ret = null;
		
		SFLog.d2("ModelFacotry"," FIND Element Type for metadat = %s" , metadata );
						
		for(SFV3ElementType s:SFV3ElementType.values())
		{
			if(metadata.endsWith(s.toString()))
			{
				ret = s;
				
				break;
			}
		}
		
		if(ret == null)
		{
			SFLog.d2("ModelFacotry"," NOT in model factory: " + metadata );
		}
		else
		{
			SFLog.d2("ModelFacotry"," Element Type = %s" , ret.toString() );
		}
		
		return ret;
	}
}