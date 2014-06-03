package com.sharefile.api.enumerations;

import com.sharefile.api.constants.SFKeywords;
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
import com.sharefile.api.models.internal.SFOutlookInformation;
import com.sharefile.api.models.internal.SFOutlookInformationOptionBool;
import com.sharefile.api.models.internal.SFOutlookInformationOptionInt;
import com.sharefile.api.models.internal.SFOutlookInformationOptionString;
import com.sharefile.java.log.SLog;

public enum SFV3FeedType
{		
	AccessControl("$metadata#AccessControls",SFAccessControl.class),	
	Account("$metadata#Account",SFAccount.class),
	AccountPreferences("$metadata#AccountPreferences",SFAccountPreferences.class),
	AccountUser("$metadata#AccountUser",SFAccountUser.class),
	AdvancedSearchResults("$metadata#AdvancedSearchResults",SFAdvancedSearchResults.class),
	AsyncOperation("$metadata#AsyncOperation",SFAsyncOperation.class),
	Capability("$metadata#Capabilities",SFCapability.class),	
	Contact("$metadata#Contact",SFContact.class),
	Device("$metadata#Device",SFDevice.class),
	DeviceLogEntry("$metadata#DeviceLogEntry",SFDeviceLogEntry.class),
	DeviceStatus("$metadata#DeviceStatus",SFDeviceStatus.class),
	DeviceUser("$metadata#DeviceUser",SFDeviceUser.class),
	DeviceUserWipe("$metadata#DeviceUserWipe",SFDeviceUserWipe.class),
	DeviceWipeReport("$metadata#DeviceWipeReport",SFDeviceWipeReport.class),
	DownloadSpecification("$metadata#DownloadSpecification",SFDownloadSpecification.class),
	FavoriteFolder("$metadata#FavoriteFolder",SFFavoriteFolder.class),
	File("$metadata#File",SFFile.class),
	FindSubdomainParams("$metadata#FindSubdomainParams",SFFindSubdomainParams.class),
	FindSubdomainResult("$metadata#FindSubdomainResult",SFFindSubdomainResult.class),
	Folder("$metadata#Folder",SFFolder.class),
	GenericConfig("$metadata#GenericConfig",SFGenericConfig.class),
	Group("$metadata#Group",SFGroup.class),
	Item("$metadata#Item",SFItem.class),
	ItemInfo("$metadata#ItemInfo",SFItemInfo.class),
	ItemProtocolLink("$metadata#ItemProtocolLink",SFItemProtocolLink.class),
	Link("$metadata#Link",SFLink.class),
	Metadata("$metadata#Metadata",SFMetadata.class),
	MobileSecuritySettings("$metadata#MobileSecuritySettings",SFMobileSecuritySettings.class),
	Note("$metadata#Note",SFNote.class),
	Notification("$metadata#Notification",SFNotification.class),	
	ODataObject("$metadata#ODataObject",SFODataObject.class),
	PlanFeatures("$metadata#PlanFeatures",SFPlanFeatures.class),
	Principal("$metadata#Principal",SFPrincipal.class),
	ProductDefaults("$metadata#ProductDefaults",SFProductDefaults.class),
	Query("$metadata#Query",SFQuery.class),
	QueryPaging("$metadata#QueryPaging",SFQueryPaging.class),
	QuerySorting("$metadata#QuerySorting",SFQuerySorting.class),
	RequireSubdomainResult("$metadata#RequireSubdomainResult",SFRequireSubdomainResult.class),
	RequireWebPopResult("$metadata#RequireWebPopResult",SFRequireWebPopResult.class),
	SearchQuery("$metadata#SearchQuery",SFSearchQuery.class),
	SearchResult("$metadata#SearchResult",SFSearchResult.class),
	SearchResults("$metadata#SearchResults",SFSearchResults.class),
	Session("$metadata#Session",SFSession.class),
	Share("$metadata#Shares",SFShare.class),
	ShareAlias("$metadata#ShareAlias",SFShareAlias.class),
	ShareRequestParams("$metadata#ShareRequestParams",SFShareRequestParams.class),
	ShareSendParams("$metadata#ShareSendParams",SFShareSendParams.class),
	SimpleQuery("$metadata#SimpleQuery",SFSimpleQuery.class),
	SimpleSearchQuery("$metadata#SimpleSearchQuery",SFSimpleSearchQuery.class),
	SSOAccountProvider("$metadata#SSOAccountProvider",SFSSOAccountProvider.class),
	SSOInfo("$metadata#SSOInfo",SFSSOInfo.class),
	SSOInfoEntry("$metadata#SSOInfoEntry",SFSSOInfoEntry.class),
	StorageCenter("$metadata#StorageCenter",SFStorageCenter.class),
	SymbolicLink("$metadata#SymbolicLink",SFSymbolicLink.class),
	UploadSpecification("$metadata#UploadSpecification",SFUploadSpecification.class),
	User("$metadata#User",SFUser.class),
	UserConfirmationSettings("$metadata#UserConfirmationSettings",SFUserConfirmationSettings.class),
	UserInfo("$metadata#UserInfo",SFUserInfo.class),
	UserPreferences("$metadata#UserPreferences",SFUserPreferences.class),
	UserSecurity("$metadata#UserSecurity",SFUserSecurity.class),
	Zone("$metadata#Zones",SFZone.class),	
	OutlookInformation("$metadata#OutlookInformation",SFOutlookInformation.class),
	OutlookInformationOptionBool("$metadata#OutlookInformationOptionBool",SFOutlookInformationOptionBool.class),
	OutlookInformationOptionInt("$metadata#OutlookInformationOptionInt",SFOutlookInformationOptionInt.class),
	OutlookInformationOptionString("$metadata#OutlookInformationOptionString",SFOutlookInformationOptionString.class);
		
	private final static String TAG = SFKeywords.TAG + "-SFV3FeedType";
	private final String mToString;
	private final Class<?> mClass;
	
	private SFV3FeedType(String toStr,Class<?> clazz)
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
	
	public static final SFV3FeedType getFeedTypeFromMetaData(String metadata)
	{
		SFV3FeedType ret = null;
		
		//SLog.d(TAG," FIND Element Type for metadata = " + metadata );
						
		if(metadata!=null && metadata.contains("$metadata#"))
		{
			for(SFV3FeedType s:SFV3FeedType.values())
			{
				if(metadata.endsWith(s.toString()))
				{
					ret = s;
					
					break;
				}
			}
			
			if(ret == null)
			{
				SLog.d(TAG," NOT in model factory: " + metadata );
			}			
		}
		
		return ret;
	}		
}