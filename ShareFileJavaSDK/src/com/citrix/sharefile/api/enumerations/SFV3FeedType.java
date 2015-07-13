package com.citrix.sharefile.api.enumerations;

import com.citrix.sharefile.api.constants.SFKeywords;
import com.citrix.sharefile.api.exceptions.SFInvalidTypeException;
import com.citrix.sharefile.api.log.Logger;
import com.citrix.sharefile.api.models.SFAccessControl;
import com.citrix.sharefile.api.models.SFAccount;
import com.citrix.sharefile.api.models.SFAccountPreferences;
import com.citrix.sharefile.api.models.SFAccountUser;
import com.citrix.sharefile.api.models.SFAdvancedSearchResults;
import com.citrix.sharefile.api.models.SFAsyncOperation;
import com.citrix.sharefile.api.models.SFCapability;
import com.citrix.sharefile.api.models.SFContact;
import com.citrix.sharefile.api.models.SFDevice;
import com.citrix.sharefile.api.models.SFDeviceLogEntry;
import com.citrix.sharefile.api.models.SFDeviceStatus;
import com.citrix.sharefile.api.models.SFDeviceUser;
import com.citrix.sharefile.api.models.SFDeviceUserWipe;
import com.citrix.sharefile.api.models.SFDeviceWipeReport;
import com.citrix.sharefile.api.models.SFDownloadSpecification;
import com.citrix.sharefile.api.models.SFFavoriteFolder;
import com.citrix.sharefile.api.models.SFFile;
import com.citrix.sharefile.api.models.SFFindSubdomainParams;
import com.citrix.sharefile.api.models.SFFindSubdomainResult;
import com.citrix.sharefile.api.models.SFFolder;
import com.citrix.sharefile.api.models.SFGenericConfig;
import com.citrix.sharefile.api.models.SFGroup;
import com.citrix.sharefile.api.models.SFItem;
import com.citrix.sharefile.api.models.SFItemInfo;
import com.citrix.sharefile.api.models.SFItemProtocolLink;
import com.citrix.sharefile.api.models.SFLink;
import com.citrix.sharefile.api.models.SFMetadata;
import com.citrix.sharefile.api.models.SFMobileSecuritySettings;
import com.citrix.sharefile.api.models.SFNote;
import com.citrix.sharefile.api.models.SFNotification;
import com.citrix.sharefile.api.models.SFODataObject;
import com.citrix.sharefile.api.models.SFOutlookInformation;
import com.citrix.sharefile.api.models.SFOutlookInformationOptionBool;
import com.citrix.sharefile.api.models.SFOutlookInformationOptionInt;
import com.citrix.sharefile.api.models.SFOutlookInformationOptionString;
import com.citrix.sharefile.api.models.SFPlanFeatures;
import com.citrix.sharefile.api.models.SFPrincipal;
import com.citrix.sharefile.api.models.SFProductDefaults;
import com.citrix.sharefile.api.models.SFQuery;
import com.citrix.sharefile.api.models.SFQueryPaging;
import com.citrix.sharefile.api.models.SFQuerySorting;
import com.citrix.sharefile.api.models.SFRequireSubdomainResult;
import com.citrix.sharefile.api.models.SFRequireWebPopResult;
import com.citrix.sharefile.api.models.SFSSOAccountProvider;
import com.citrix.sharefile.api.models.SFSSOInfo;
import com.citrix.sharefile.api.models.SFSSOInfoEntry;
import com.citrix.sharefile.api.models.SFSearchQuery;
import com.citrix.sharefile.api.models.SFSearchResult;
import com.citrix.sharefile.api.models.SFSearchResults;
import com.citrix.sharefile.api.models.SFSession;
import com.citrix.sharefile.api.models.SFShare;
import com.citrix.sharefile.api.models.SFShareAlias;
import com.citrix.sharefile.api.models.SFShareRequestParams;
import com.citrix.sharefile.api.models.SFShareSendParams;
import com.citrix.sharefile.api.models.SFSimpleQuery;
import com.citrix.sharefile.api.models.SFSimpleSearchQuery;
import com.citrix.sharefile.api.models.SFStorageCenter;
import com.citrix.sharefile.api.models.SFSymbolicLink;
import com.citrix.sharefile.api.models.SFUploadSpecification;
import com.citrix.sharefile.api.models.SFUser;
import com.citrix.sharefile.api.models.SFUserConfirmationSettings;
import com.citrix.sharefile.api.models.SFUserInfo;
import com.citrix.sharefile.api.models.SFUserPreferences;
import com.citrix.sharefile.api.models.SFUserSecurity;
import com.citrix.sharefile.api.models.SFZone;

public enum SFV3FeedType
{		
	AccessControl("$metadata#AccessControls",SFAccessControl.class),	
	Account("$metadata#Account",SFAccount.class),
    Accounts("$metadata#Accounts",SFAccount.class),
	AccountPreferences("$metadata#AccountPreferences",SFAccountPreferences.class),
	AccountUser("$metadata#AccountUser",SFAccountUser.class),
	AdvancedSearchResults("$metadata#AdvancedSearchResults",SFAdvancedSearchResults.class),
	AsyncOperation("$metadata#AsyncOperation",SFAsyncOperation.class),
	Capability("$metadata#Capabilities",SFCapability.class),	
	Contact("$metadata#Contact",SFContact.class),
	Contacts("$metadata#Contacts",SFContact.class),
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
	Groups("$metadata#Groups",SFGroup.class),
	Item("$metadata#Item",SFItem.class),
    Items("$metadata#Items",SFItem.class),
	ItemInfo("$metadata#ItemInfo",SFItemInfo.class),
	ItemProtocolLink("$metadata#ItemProtocolLinks",SFItemProtocolLink.class),
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
	StorageCenter("$metadata#StorageCenters",SFStorageCenter.class),
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
	private final Class<?> mOriginalClass;
	private Class<?> mOverrideClass;
	
	private SFV3FeedType(String toStr,Class<?> clazz)
	{
		mToString = toStr;
		mOriginalClass = clazz;
		mOverrideClass = mOriginalClass;
	}
	
	@Override
	public String toString() 
	{		
		return mToString;
	}
	
	public Class<?> getV3Class()
	{
		return mOverrideClass;
	}
	
	public static final SFV3FeedType getFeedTypeFromMetaData(String metadata)
	{
		SFV3FeedType ret = null;
		
		//Logger.d(TAG," FIND Element Type for metadata = " + metadata );
						
		if(metadata!=null && metadata.contains("$metadata#"))
		{
			for(SFV3FeedType s:SFV3FeedType.values())
			{
				if(metadata.contains(s.toString()))
				{
					ret = s;
					
					break;
				}
			}
			
			if(ret == null)
			{
				Logger.d(TAG," NOT in model factory: " + metadata );
			}			
		}
		
		return ret;
	}	
	
	/** 
	 *  We are allowing consumers of the SDK to register their own deriived classes from the base models 
	 *  we have inside the SDK. This allows for cases where the consumer wants to add addtional flags and functions
	 *  to the model and yet have orginal parsed objects of his liking. Example SFFile does not provide the isSynced
	 *  flag. The consumer app can extend like : 
	 *  	<p>SFFileEx extends SFFile 
	 *  	<p>{ 
	 *  	<p>	boolean mIsSync
	 *  	<p>}
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SFInvalidTypeException 
	 */
	public static void registerSubClass(SFV3FeedType feedType, Class<?> newClass) throws InstantiationException, IllegalAccessException, SFInvalidTypeException
	{
		if(newClass == null)
		{
			throw new SFInvalidTypeException(" NULL does not extend " + feedType.mOriginalClass.toString());
		}
				 						
		//test if the new class is a real extension of the type being replaced.
		if(!feedType.mOriginalClass.isInstance(newClass.newInstance()))
		{
			String msg = newClass.toString() + " does not extend " + feedType.mOriginalClass.toString();
			
			Logger.d(TAG, msg);
			
			throw new SFInvalidTypeException(msg);
		}
		
		Logger.d(TAG, "Successfully registered : " + newClass.toString() + " to replace " + feedType.mOriginalClass.toString());
		
		feedType.mOverrideClass = newClass;
	}
}