package com.sharefile.api.utils;

import com.sharefile.api.models.SFAccessControlFilter;
import com.sharefile.api.models.SFAppCodes;
import com.sharefile.api.models.SFAppStore;
import com.sharefile.api.models.SFAsyncOperationState;
import com.sharefile.api.models.SFAsyncOperationType;
import com.sharefile.api.models.SFCapabilityName;
import com.sharefile.api.models.SFConnectorGroupKind;
import com.sharefile.api.models.SFDeviceActionInitiatorRole;
import com.sharefile.api.models.SFDeviceLogEntryAction;
import com.sharefile.api.models.SFESignatureDocumentStatus;
import com.sharefile.api.models.SFEnsEventType;
import com.sharefile.api.models.SFFileVirusStatus;
import com.sharefile.api.models.SFIntegrationProvider;
import com.sharefile.api.models.SFLockType;
import com.sharefile.api.models.SFMobileUserRole;
import com.sharefile.api.models.SFOAuthClientPermissions;
import com.sharefile.api.models.SFOAuthState;
import com.sharefile.api.models.SFODataObjectType;
import com.sharefile.api.models.SFPinLockType;
import com.sharefile.api.models.SFPlanAddonsStatus;
import com.sharefile.api.models.SFPreviewPlatform;
import com.sharefile.api.models.SFPreviewStatus;
import com.sharefile.api.models.SFQueueStatus;
import com.sharefile.api.models.SFSFTool;
import com.sharefile.api.models.SFShareConnectAddonFeatureInfo;
import com.sharefile.api.models.SFShareSubType;
import com.sharefile.api.models.SFShareType;
import com.sharefile.api.models.SFTreeMode;
import com.sharefile.api.models.SFUXMode;
import com.sharefile.api.models.SFUploadMethod;
import com.sharefile.api.models.SFUserRole;
import com.sharefile.api.models.SFVRootType;
import com.sharefile.api.models.SFZoneService;
import com.sharefile.api.models.SFZoneType;
import com.sharefile.java.log.SLog;

public class SafeEnumHelpers 
{
	private static final int BEGIN_INDEX_SAFE_ENUM = "com.sharefile.api.enumerations.SFSafeEnum<com.sharefile.api.models.".length();
    private static final int BEGIN_INDEX_SAFE_ENUM_FLAGS = "com.sharefile.api.enumerations.SFSafeEnumFlags<com.sharefile.api.models.".length();
	
	private static class EnumClassNames
	{		
		public EnumClassNames(String string, Class clazz) 
		{
			mStrName = string;
			mClassName = clazz;
		}
		
		public final String mStrName;
		public final Class mClassName;
	};
	
	private static final EnumClassNames[] mEnumClassNames = new EnumClassNames[]
	{
		new EnumClassNames("SFFileVirusStatus",SFFileVirusStatus.class),
		new EnumClassNames("SFPreviewStatus",SFPreviewStatus.class),
        new EnumClassNames("SFShareType",SFShareType.class),
        new EnumClassNames("SFCapabilityName",SFCapabilityName.class),
        new EnumClassNames("SFZoneType",SFZoneType.class),
        new EnumClassNames("SFVRootType",SFVRootType.class),
        new EnumClassNames("SFZoneService",SFZoneService.class),
        new EnumClassNames("SFPreviewPlatform",SFPreviewPlatform.class),
        new EnumClassNames("SFAccessControlFilter",SFAccessControlFilter.class),
        new EnumClassNames("SFAppCodes",SFAppCodes.class),
        new EnumClassNames("SFAppStore",SFAppStore.class),
        new EnumClassNames("SFAsyncOperationState",SFAsyncOperationState.class),
        new EnumClassNames("SFAsyncOperationType",SFAsyncOperationType.class),
        new EnumClassNames("SFConnectorGroupKind",SFConnectorGroupKind.class),
        new EnumClassNames("SFDeviceActionInitiatorRole",SFDeviceActionInitiatorRole.class),
        new EnumClassNames("SFDeviceLogEntryAction",SFDeviceLogEntryAction.class),
        new EnumClassNames("SFEnsEventType",SFEnsEventType.class),
        new EnumClassNames("SFESignatureDocumentStatus",SFESignatureDocumentStatus.class),
        new EnumClassNames("SFIntegrationProvider",SFIntegrationProvider.class),
        new EnumClassNames("SFLockType",SFLockType.class),
        new EnumClassNames("SFMobileUserRole",SFMobileUserRole.class),
        new EnumClassNames("SFOAuthClientPermissions",SFOAuthClientPermissions.class),
        new EnumClassNames("SFOAuthState",SFOAuthState.class),
        new EnumClassNames("SFODataObjectType",SFODataObjectType.class),
        new EnumClassNames("SFPinLockType",SFPinLockType.class),
        new EnumClassNames("SFPlanAddonsStatus",SFPlanAddonsStatus.class),
        new EnumClassNames("SFQueueStatus",SFQueueStatus.class),
        new EnumClassNames("SFSFTool",SFSFTool.class),
        new EnumClassNames("SFShareConnectAddonFeatureInfo",SFShareConnectAddonFeatureInfo.class),
        new EnumClassNames("SFShareSubType",SFShareSubType.class),
        new EnumClassNames("SFTreeMode",SFTreeMode.class),
        new EnumClassNames("SFUploadMethod",SFUploadMethod.class),
        new EnumClassNames("SFUserRole",SFUserRole.class),
        new EnumClassNames("SFUXMode",SFUXMode.class)
	};
		
	public static Class getEnumClass(String className, boolean useEnumWithFlags)
	{
		Class clazz = null;

        int beginIndex = useEnumWithFlags?BEGIN_INDEX_SAFE_ENUM_FLAGS:BEGIN_INDEX_SAFE_ENUM;

		if(className!=null && className.length()> beginIndex)
		{
			String containedClassName = className.substring(beginIndex, className.length()-1);
			
			for(EnumClassNames e:mEnumClassNames)
			{
				if(e.mStrName.equals(containedClassName))
				{
					clazz =  e.mClassName;
					break;
				}
			}					
		}
		
		return clazz;
	}
	
	public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string)
	{
	    if( c != null && string != null )
	    {
	        try
	        {
	            return Enum.valueOf(c, string);
	        }
	        catch(IllegalArgumentException ex)
	        {
                //SLog.d("SafeEnumHelper",ex.getLocalizedMessage());
	        }
	    }	    
	    return null;
	}
}