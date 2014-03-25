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

public enum SFV3FeedType
{		
	AccessControl{@Override public String toString() {return "$metadata#AccessControls";}  @Override public Class getV3Class(){return SFAccessControl.class;}},	
	Account{@Override public String toString() {return "$metadata#Account";}  @Override public Class getV3Class(){return SFAccount.class;}},
	AccountPreferences{@Override public String toString() {return "$metadata#AccountPreferences";}  @Override public Class getV3Class(){return SFAccountPreferences.class;}},
	AccountUser{@Override public String toString() {return "$metadata#AccountUser";}  @Override public Class getV3Class(){return SFAccountUser.class;}},
	AdvancedSearchResults{@Override public String toString() {return "$metadata#AdvancedSearchResults";}  @Override public Class getV3Class(){return SFAdvancedSearchResults.class;}},
	AsyncOperation{@Override public String toString() {return "$metadata#AsyncOperation";}  @Override public Class getV3Class(){return SFAsyncOperation.class;}},
	Capability{@Override public String toString() {return "$metadata#Capabilities";}  @Override public Class getV3Class(){return SFCapability.class;}},	
	Contact{@Override public String toString() {return "$metadata#Contact";}  @Override public Class getV3Class(){return SFContact.class;}},
	Device{@Override public String toString() {return "$metadata#Device";}  @Override public Class getV3Class(){return SFDevice.class;}},
	DeviceLogEntry{@Override public String toString() {return "$metadata#DeviceLogEntry";}  @Override public Class getV3Class(){return SFDeviceLogEntry.class;}},
	DeviceStatus{@Override public String toString() {return "$metadata#DeviceStatus";}  @Override public Class getV3Class(){return SFDeviceStatus.class;}},
	DeviceUser{@Override public String toString() {return "$metadata#DeviceUser";}  @Override public Class getV3Class(){return SFDeviceUser.class;}},
	DeviceUserWipe{@Override public String toString() {return "$metadata#DeviceUserWipe";}  @Override public Class getV3Class(){return SFDeviceUserWipe.class;}},
	DeviceWipeReport{@Override public String toString() {return "$metadata#DeviceWipeReport";}  @Override public Class getV3Class(){return SFDeviceWipeReport.class;}},
	DownloadSpecification{@Override public String toString() {return "$metadata#DownloadSpecification";}  @Override public Class getV3Class(){return SFDownloadSpecification.class;}},
	FavoriteFolder{@Override public String toString() {return "$metadata#FavoriteFolder";}  @Override public Class getV3Class(){return SFFavoriteFolder.class;}},
	File{@Override public String toString() {return "$metadata#File";}  @Override public Class getV3Class(){return SFFile.class;}},
	FindSubdomainParams{@Override public String toString() {return "$metadata#FindSubdomainParams";}  @Override public Class getV3Class(){return SFFindSubdomainParams.class;}},
	FindSubdomainResult{@Override public String toString() {return "$metadata#FindSubdomainResult";}  @Override public Class getV3Class(){return SFFindSubdomainResult.class;}},
	Folder{@Override public String toString() {return "$metadata#Folder";}  @Override public Class getV3Class(){return SFFolder.class;}},
	GenericConfig{@Override public String toString() {return "$metadata#GenericConfig";}  @Override public Class getV3Class(){return SFGenericConfig.class;}},
	Group{@Override public String toString() {return "$metadata#Group";}  @Override public Class getV3Class(){return SFGroup.class;}},
	Item{@Override public String toString() {return "$metadata#Item";}  @Override public Class getV3Class(){return SFItem.class;}},
	ItemInfo{@Override public String toString() {return "$metadata#ItemInfo";}  @Override public Class getV3Class(){return SFItemInfo.class;}},
	ItemProtocolLink{@Override public String toString() {return "$metadata#ItemProtocolLink";}  @Override public Class getV3Class(){return SFItemProtocolLink.class;}},
	Link{@Override public String toString() {return "$metadata#Link";}  @Override public Class getV3Class(){return SFLink.class;}},
	Metadata{@Override public String toString() {return "$metadata#Metadata";}  @Override public Class getV3Class(){return SFMetadata.class;}},
	MobileSecuritySettings{@Override public String toString() {return "$metadata#MobileSecuritySettings";}  @Override public Class getV3Class(){return SFMobileSecuritySettings.class;}},
	Note{@Override public String toString() {return "$metadata#Note";}  @Override public Class getV3Class(){return SFNote.class;}},
	Notification{@Override public String toString() {return "$metadata#Notification";}  @Override public Class getV3Class(){return SFNotification.class;}},	
	ODataObject{@Override public String toString() {return "$metadata#ODataObject";}  @Override public Class getV3Class(){return SFODataObject.class;}},
	PlanFeatures{@Override public String toString() {return "$metadata#PlanFeatures";}  @Override public Class getV3Class(){return SFPlanFeatures.class;}},
	Principal{@Override public String toString() {return "$metadata#Principal";}  @Override public Class getV3Class(){return SFPrincipal.class;}},
	ProductDefaults{@Override public String toString() {return "$metadata#ProductDefaults";}  @Override public Class getV3Class(){return SFProductDefaults.class;}},
	Query{@Override public String toString() {return "$metadata#Query";}  @Override public Class getV3Class(){return SFQuery.class;}},
	QueryPaging{@Override public String toString() {return "$metadata#QueryPaging";}  @Override public Class getV3Class(){return SFQueryPaging.class;}},
	QuerySorting{@Override public String toString() {return "$metadata#QuerySorting";}  @Override public Class getV3Class(){return SFQuerySorting.class;}},
	RequireSubdomainResult{@Override public String toString() {return "$metadata#RequireSubdomainResult";}  @Override public Class getV3Class(){return SFRequireSubdomainResult.class;}},
	RequireWebPopResult{@Override public String toString() {return "$metadata#RequireWebPopResult";}  @Override public Class getV3Class(){return SFRequireWebPopResult.class;}},
	SearchQuery{@Override public String toString() {return "$metadata#SearchQuery";}  @Override public Class getV3Class(){return SFSearchQuery.class;}},
	SearchResult{@Override public String toString() {return "$metadata#SearchResult";}  @Override public Class getV3Class(){return SFSearchResult.class;}},
	SearchResults{@Override public String toString() {return "$metadata#SearchResults";}  @Override public Class getV3Class(){return SFSearchResults.class;}},
	Session{@Override public String toString() {return "$metadata#Session";}  @Override public Class getV3Class(){return SFSession.class;}},
	Share{@Override public String toString() {return "$metadata#Shares";}  @Override public Class getV3Class(){return SFShare.class;}},
	ShareAlias{@Override public String toString() {return "$metadata#ShareAlias";}  @Override public Class getV3Class(){return SFShareAlias.class;}},
	ShareRequestParams{@Override public String toString() {return "$metadata#ShareRequestParams";}  @Override public Class getV3Class(){return SFShareRequestParams.class;}},
	ShareSendParams{@Override public String toString() {return "$metadata#ShareSendParams";}  @Override public Class getV3Class(){return SFShareSendParams.class;}},
	SimpleQuery{@Override public String toString() {return "$metadata#SimpleQuery";}  @Override public Class getV3Class(){return SFSimpleQuery.class;}},
	SimpleSearchQuery{@Override public String toString() {return "$metadata#SimpleSearchQuery";}  @Override public Class getV3Class(){return SFSimpleSearchQuery.class;}},
	SSOAccountProvider{@Override public String toString() {return "$metadata#SSOAccountProvider";}  @Override public Class getV3Class(){return SFSSOAccountProvider.class;}},
	SSOInfo{@Override public String toString() {return "$metadata#SSOInfo";}  @Override public Class getV3Class(){return SFSSOInfo.class;}},
	SSOInfoEntry{@Override public String toString() {return "$metadata#SSOInfoEntry";}  @Override public Class getV3Class(){return SFSSOInfoEntry.class;}},
	StorageCenter{@Override public String toString() {return "$metadata#StorageCenter";}  @Override public Class getV3Class(){return SFStorageCenter.class;}},
	SymbolicLink{@Override public String toString() {return "$metadata#SymbolicLink";}  @Override public Class getV3Class(){return SFSymbolicLink.class;}},
	UploadSpecification{@Override public String toString() {return "$metadata#UploadSpecification";}  @Override public Class getV3Class(){return SFUploadSpecification.class;}},
	User{@Override public String toString() {return "$metadata#User";}  @Override public Class getV3Class(){return SFUser.class;}},
	UserConfirmationSettings{@Override public String toString() {return "$metadata#UserConfirmationSettings";}  @Override public Class getV3Class(){return SFUserConfirmationSettings.class;}},
	UserInfo{@Override public String toString() {return "$metadata#UserInfo";}  @Override public Class getV3Class(){return SFUserInfo.class;}},
	UserPreferences{@Override public String toString() {return "$metadata#UserPreferences";}  @Override public Class getV3Class(){return SFUserPreferences.class;}},
	UserSecurity{@Override public String toString() {return "$metadata#UserSecurity";}  @Override public Class getV3Class(){return SFUserSecurity.class;}},
	Zone{@Override public String toString() {return "$metadata#Zones";}  @Override public Class getV3Class(){return SFZone.class;}},	
	OutlookInformation{@Override public String toString() {return "$metadata#OutlookInformation";}  @Override public Class getV3Class(){return SFOutlookInformation.class;}},
	OutlookInformationOptionBool{@Override public String toString() {return "$metadata#OutlookInformationOptionBool";}  @Override public Class getV3Class(){return SFOutlookInformationOptionBool.class;}},
	OutlookInformationOptionInt{@Override public String toString() {return "$metadata#OutlookInformationOptionInt";}  @Override public Class getV3Class(){return SFOutlookInformationOptionInt.class;}},
	OutlookInformationOptionString{@Override public String toString() {return "$metadata#OutlookInformationOptionString";}  @Override public Class getV3Class(){return SFOutlookInformationOptionString.class;}};
		
	public abstract Class getV3Class();
	
	public static final SFV3FeedType getFeedTypeFromMetaData(String metadata)
	{
		SFV3FeedType ret = null;
		
		SFLog.d2("ModelFacotry"," FIND Element Type for metadata = %s" , metadata );
						
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
				SFLog.d2("ModelFacotry"," NOT in model factory: " + metadata );
			}
			else
			{
				SFLog.d2("ModelFacotry"," Feed Type = %s" , ret.toString() );
			}
		}
		
		return ret;
	}		
}