package com.sharefile.api.enumerations;

import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.exceptions.SFInvalidTypeException;
import com.sharefile.api.gson.auto.SFDefaultGsonParser;
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
import com.sharefile.api.models.SFRedirection;
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
import com.sharefile.api.log.Logger;

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
	ItemInfo("Models.ItemInfo",SFItemInfo.class),
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
	Redirection("Models.Redirection",SFRedirection.class),
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
	//The v3 API is returning conflicting specs for UploadSpecification. Lets map both of them to the same class
	UploadSpecificationOld("Models.UploadSpecification@Element",SFUploadSpecification.class),
	UploadSpecification("Models.UploadSpecification",SFUploadSpecification.class),
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
			
	private static final String TAG = SFKeywords.TAG + "-SFV3ElementType";
	private final String mToString;
	private final Class<?> mOriginalClass;//This is the one originally intended by the SDK
	private Class<?> mOverrideClass;// This is the one that can be overriden by the consumer app.
	
	private SFV3ElementType(String toStr,Class<?> clazz)
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

    /**
        returns the ShareFile type string as required by the odata.type
     */
    public String type()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("ShareFile.Api.");

        int index = mToString.indexOf("@");

        if(index>0)
        {
            sb.append(mToString.substring(0, index));
        }
        else
        {
            sb.append(mToString);
        }

        return sb.toString();
    }
	
	public Class<?> getV3Class()
	{
		return mOverrideClass;
	}
	
	/** 
	 *  We are allowing consumers of the SDK to register their own deriived classes from the base models 
	 *  we have inside the SDK. This allows for cases where the consumer wants to add addtional flags and functions
	 *  to the model and yet have orginal parsed objects of his liking. Example SFFile does not provide the isSynced
	 *  flag. The consumer app can extend like : 
	 *  	<p>SFFileEx extends SFFile 
	 *  	<br>{ 
	 *  	<br>	boolean mIsSync
	 *  	<br>}
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SFInvalidTypeException 
	 */
	public static void registerSubClass(SFV3ElementType elementType, Class<?> newClass) throws InstantiationException, IllegalAccessException, SFInvalidTypeException
	{
		if(newClass == null)
		{
			throw new SFInvalidTypeException(" NULL does not extend " + elementType.mOriginalClass.toString());
		}
				 						
		//test if the new class is a real extension of the type being replaced.
		if(!elementType.mOriginalClass.isInstance(newClass.newInstance()))
		{
			String msg = newClass.toString() + " does not extend " + elementType.mOriginalClass.toString();
			
			Logger.d(TAG, msg);
			
			throw new SFInvalidTypeException(msg);
		}
		
		Logger.d(TAG, "Successfully registered : " + newClass.toString() + " to replace " + elementType.mOriginalClass.toString());
		
		elementType.mOverrideClass = newClass;

        /*
            This is required so that we call our custom Gson parser which knows about instantiating
            the user supplied class instead of the base type.
         */
        SFDefaultGsonParser.registerTypeAdapter(elementType.mOriginalClass);
	}
	
	public static boolean isFolderType(SFODataObject object)
	{
		boolean ret = false;
		
		if(object!=null)
		{
			if(object instanceof SFFolder)
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
	
	public static boolean isNoteType(SFODataObject object)
	{
		boolean ret = false;
		
		if(object!=null)
		{
			if(object instanceof SFNote)
			{
				ret = true;
			}
		}
		
		return ret;
	}
	
	public static boolean isLinkType(SFODataObject object)
	{
		boolean ret = false;
		
		if(object!=null)
		{
			if(object instanceof SFLink)
			{
				ret = true;
			}
		}
		
		return ret;
	}
	
	public static boolean isSymbolicLinkType(SFODataObject object)
	{
		boolean ret = false;
		
		if(object!=null)
		{
			if(object instanceof SFSymbolicLink)
			{
				ret = true;
			}
		}
		
		return ret;
	}
			
	public static final SFV3ElementType getElementTypeFromMetaData(String metadata)
	{
		SFV3ElementType ret = null;
		
		//Logger.d(TAG, "FIND Element Type for metadata = " + metadata );
						
		//if(metadata!=null && metadata.contains("Models.") && metadata.contains("@Element"))
		//metadata.contains("@Element") is not correct for ItemsInfo
		if(metadata!=null && metadata.contains("Models."))
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
				Logger.d(TAG, " NOT in model factory: " + metadata );
			}			
		}
		
		return ret;
	}		
}