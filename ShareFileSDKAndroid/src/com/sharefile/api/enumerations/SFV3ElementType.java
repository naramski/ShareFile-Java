package com.sharefile.api.enumerations;

import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.models.SFAccessControl;
import com.sharefile.api.models.SFAccount;
import com.sharefile.api.models.SFAccountPreferences;
import com.sharefile.api.models.SFAccountUser;
import com.sharefile.api.models.SFAdvancedSearchResults;
import com.sharefile.api.models.SFAsyncOperation;
import com.sharefile.api.models.SFCapability;
import com.sharefile.api.models.SFContact;
import com.sharefile.api.models.SFDevice;
import com.sharefile.api.models.SFDeviceLogEntry;
import com.sharefile.api.models.SFDeviceStatus;
import com.sharefile.api.models.SFDeviceUser;
import com.sharefile.api.models.SFDeviceUserWipe;
import com.sharefile.api.models.SFDeviceWipeReport;
import com.sharefile.api.models.SFDownloadSpecification;
import com.sharefile.api.models.SFFavoriteFolder;
import com.sharefile.api.models.SFFile;
import com.sharefile.api.models.SFFindSubdomainParams;
import com.sharefile.api.models.SFFindSubdomainResult;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFGenericConfig;
import com.sharefile.api.models.SFGroup;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFItemInfo;
import com.sharefile.api.models.SFItemProtocolLink;
import com.sharefile.api.models.SFLink;
import com.sharefile.api.models.SFMetadata;
import com.sharefile.api.models.SFMobileSecuritySettings;
import com.sharefile.api.models.SFNote;
import com.sharefile.api.models.SFNotification;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFPlanFeatures;
import com.sharefile.api.models.SFPrincipal;
import com.sharefile.api.models.SFProductDefaults;
import com.sharefile.api.models.SFQuery;
import com.sharefile.api.models.SFQueryPaging;
import com.sharefile.api.models.SFQuerySorting;
import com.sharefile.api.models.SFRequireSubdomainResult;
import com.sharefile.api.models.SFRequireWebPopResult;
import com.sharefile.api.models.SFSSOAccountProvider;
import com.sharefile.api.models.SFSSOInfo;
import com.sharefile.api.models.SFSSOInfoEntry;
import com.sharefile.api.models.SFSearchQuery;
import com.sharefile.api.models.SFSearchResult;
import com.sharefile.api.models.SFSearchResults;
import com.sharefile.api.models.SFSession;
import com.sharefile.api.models.SFShare;
import com.sharefile.api.models.SFShareAlias;
import com.sharefile.api.models.SFShareRequestParams;
import com.sharefile.api.models.SFShareSendParams;
import com.sharefile.api.models.SFSimpleQuery;
import com.sharefile.api.models.SFSimpleSearchQuery;
import com.sharefile.api.models.SFStorageCenter;
import com.sharefile.api.models.SFSymbolicLink;
import com.sharefile.api.models.SFUploadSpecification;
import com.sharefile.api.models.SFUser;
import com.sharefile.api.models.SFUserConfirmationSettings;
import com.sharefile.api.models.SFUserInfo;
import com.sharefile.api.models.SFUserPreferences;
import com.sharefile.api.models.SFUserSecurity;
import com.sharefile.api.models.SFZone;
import com.sharefile.api.models.Private.SFOutlookInformation;
import com.sharefile.api.models.Private.SFOutlookInformationOptionBool;
import com.sharefile.api.models.Private.SFOutlookInformationOptionInt;
import com.sharefile.api.models.Private.SFOutlookInformationOptionString;

public enum SFV3ElementType
{							
	AccessControl("Models.AccessControl@Element",SFAccessControl.class),	
	Account("Models.Account@Element",SFAccount.class),
	AccountPreferences("Models.AccountPreferences@Element",SFAccountPreferences.class),
	AccountUser("Models.AccountUser@Element",SFAccountUser.class),
	AdvancedSearchResults("Models.AdvancedSearchResults@Element",SFAdvancedSearchResults.class),
	AsyncOperation("Models.AsyncOperation@Element",SFAsyncOperation.class),
	Capability("Models.Capabilities@Element",SFCapability.class),	
	Contact("Models.Contact@Element",SFContact.class),
	Device("Models.Device@Element",SFDevice.class),
	DeviceLogEntry("Models.DeviceLogEntry@Element",SFDeviceLogEntry.class),
	DeviceStatus("Models.DeviceStatus@Element",SFDeviceStatus.class),
	DeviceUser("Models.DeviceUser@Element",SFDeviceUser.class),
	DeviceUserWipe("Models.DeviceUserWipe@Element",SFDeviceUserWipe.class),
	DeviceWipeReport("Models.DeviceWipeReport@Element",SFDeviceWipeReport.class),
	DownloadSpecification("Models.DownloadSpecification@Element",SFDownloadSpecification.class),
	FavoriteFolder("Models.FavoriteFolder@Element",SFFavoriteFolder.class),
	File("Models.File@Element",SFFile.class),
	FindSubdomainParams("Models.FindSubdomainParams@Element",SFFindSubdomainParams.class),
	FindSubdomainResult("Models.FindSubdomainResult@Element",SFFindSubdomainResult.class),
	Folder("Models.Folder@Element",SFFolder.class),
	GenericConfig("Models.GenericConfig@Element",SFGenericConfig.class),
	Group("Models.Group@Element",SFGroup.class),
	Item("Models.Item@Element",SFItem.class),
	ItemInfo("Models.ItemInfo@Element",SFItemInfo.class),
	ItemProtocolLink("Models.ItemProtocolLink@Element",SFItemProtocolLink.class),
	Link("Models.Link@Element",SFLink.class),
	Metadata("Models.Metadata@Element",SFMetadata.class),
	MobileSecuritySettings("Models.MobileSecuritySettings@Element",SFMobileSecuritySettings.class),
	Note("Models.Note@Element",SFNote.class),
	Notification("Models.Notification@Element",SFNotification.class),	
	ODataObject("Models.ODataObject@Element",SFODataObject.class),
	PlanFeatures("Models.PlanFeatures@Element",SFPlanFeatures.class),
	Principal("Models.Principal@Element",SFPrincipal.class),
	ProductDefaults("Models.ProductDefaults@Element",SFProductDefaults.class),
	Query("Models.Query@Element",SFQuery.class),
	QueryPaging("Models.QueryPaging@Element",SFQueryPaging.class),
	QuerySorting("Models.QuerySorting@Element",SFQuerySorting.class),
	RequireSubdomainResult("Models.RequireSubdomainResult@Element",SFRequireSubdomainResult.class),
	RequireWebPopResult("Models.RequireWebPopResult@Element",SFRequireWebPopResult.class),
	SearchQuery("Models.SearchQuery@Element",SFSearchQuery.class),
	SearchResult("Models.SearchResult@Element",SFSearchResult.class),
	SearchResults("Models.SearchResults@Element",SFSearchResults.class),
	Session("Models.Session@Element",SFSession.class),
	Share("Models.Share@Element",SFShare.class),
	ShareAlias("Models.ShareAlias@Element",SFShareAlias.class),
	ShareRequestParams("Models.ShareRequestParams@Element",SFShareRequestParams.class),
	ShareSendParams("Models.ShareSendParams@Element",SFShareSendParams.class),
	SimpleQuery("Models.SimpleQuery@Element",SFSimpleQuery.class),
	SimpleSearchQuery("Models.SimpleSearchQuery@Element",SFSimpleSearchQuery.class),
	SSOAccountProvider("Models.SSOAccountProvider@Element",SFSSOAccountProvider.class),
	SSOInfo("Models.SSOInfo@Element",SFSSOInfo.class),
	SSOInfoEntry("Models.SSOInfoEntry@Element",SFSSOInfoEntry.class),
	StorageCenter("Models.StorageCenter@Element",SFStorageCenter.class),
	SymbolicLink("Models.SymbolicLink@Element",SFSymbolicLink.class),
	UploadSpecification("Models.UploadSpecification@Element",SFUploadSpecification.class),
	User("Models.User@Element",SFUser.class),
	UserConfirmationSettings("Models.UserConfirmationSettings@Element",SFUserConfirmationSettings.class),
	UserInfo("Models.UserInfo@Element",SFUserInfo.class),
	UserPreferences("Models.UserPreferences@Element",SFUserPreferences.class),
	UserSecurity("Models.UserSecurity@Element",SFUserSecurity.class),
	Zone("Models.Zone@Element",SFZone.class),	
	OutlookInformation("Models.OutlookInformation@Element",SFOutlookInformation.class),
	OutlookInformationOptionBool("Models.OutlookInformationOptionBool@Element",SFOutlookInformationOptionBool.class),
	OutlookInformationOptionInt("Models.OutlookInformationOptionInt@Element",SFOutlookInformationOptionInt.class),
	OutlookInformationOptionString("Models.OutlookInformationOptionString@Element",SFOutlookInformationOptionString.class);
			
	private final String mToString;
	private final Class<?> mClass;
	
	private SFV3ElementType(String toStr,Class<?> clazz)
	{
		mToString = toStr;
		mClass = clazz;
	}
	
	@Override
	public String toString() 
	{		
		return mToString;
	}
	
	public Class<?> getV3Class()
	{
		return mClass;
	}
	
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
	
	public static boolean isFileType(SFODataObject object)
	{
		boolean ret = false;
		
		if(object!=null)
		{
			if(object instanceof SFFile)
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
						
		if(metadata!=null && metadata.contains("Models.") && metadata.contains("@Element"))			
		{
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
		}
		
		return ret;
	}		
}