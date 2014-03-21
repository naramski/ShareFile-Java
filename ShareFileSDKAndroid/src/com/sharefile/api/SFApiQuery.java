package com.sharefile.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.widget.Switch;

import com.sharefile.api.android.utils.SFLog;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.enumerations.SFProvider;
import com.sharefile.api.exceptions.SFToDoReminderException;
import com.sharefile.api.gson.auto.SFDefaultGsonParser;
import com.sharefile.api.models.SFAccessControl;
import com.sharefile.api.models.SFAccount;
import com.sharefile.api.models.SFCapability;
import com.sharefile.api.models.SFItem;
import com.sharefile.api.models.SFODataFeed;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSearchResults;
import com.sharefile.api.models.SFSession;
import com.sharefile.api.models.SFShare;
import com.sharefile.api.models.SFTreeMode;
import com.sharefile.api.models.SFUploadMethod;
import com.sharefile.api.models.SFUser;
import com.sharefile.api.models.SFVRootType;
import com.sharefile.api.models.SFZone;
import com.sharefile.api.models.SFZoneService;

public class SFApiQuery<T extends SFODataObject> 
{
	
	private static final Map<String, Class> mMapNameClassPair;
	//Fill this on need basis.
	static 
	{
	        Map<String, Class> aMap = new HashMap<String, Class>();
	        
	        aMap.put("Items", SFItem.class);
	        aMap.put("Sessions", SFSession.class);	        	        
	        aMap.put("AccessControls", SFAccessControl.class);
	        aMap.put("Capabilities", SFODataFeed.class);
	        aMap.put("Shares", SFShare.class);
	        aMap.put("User", SFUser.class);
	        aMap.put("Accounts", SFAccount.class);
	        aMap.put("Zones", SFZone.class);
	        
	        mMapNameClassPair = Collections.unmodifiableMap(aMap);
	}
	
	/**
	 * https://server/provider/version/entity(id)
	 * 
	 * https://myaccount.sf-api.com/sf/v3/Items(id)
	 *
	 */
	private String mFromEntity = null;
	private String mAction = null;
	private String mHttpMethod = null;
	private SFProvider mProvider = SFProvider.PROVIDER_TYPE_SF;
	private String mVersion = SFSDK.VERSION_FOR_QUERY_URL;
	private String mId = null;
	private Map<String,String> mQueryMap = new HashMap<String, String>();
	private Map<String,String> mIdMap = new HashMap<String, String>();
	private Class mInnerClass = null;
	private String mBody = null;
	private URI mLink = null; //The URL link obtained for V3connectors from their symbolic link or 302 redirect.
	
		
			
	public final Class getTrueInnerClass()
	{
		return mInnerClass;
	}
	
	public final void setFrom(String setFrom)
	{
		mFromEntity = setFrom;
		
		mInnerClass = mMapNameClassPair.get(setFrom);
		
		if(mInnerClass == null)
		{
			SFToDoReminderException.throwTODOException("Put the class in the map : " + setFrom);
		}
	}
	
	public final void setLink(String link) throws URISyntaxException
	{
		mLink = new URI(link);
	}
	
	public final void setProvider(SFProvider provider)
	{
		mProvider = provider;
	}
	
	public final void setVersion(String version)
	{
		mVersion = version;
	}
	
	public final void setAction(String action)
	{
		mAction = action;
	}
	
	public final void setHttpMethod(SFHttpMethod httpMethod)
	{
		mHttpMethod = httpMethod.toString();
	}
	
	/**
	 *  setId() and addIds() are mutually exclusive. We will throw and exception if both are called on the same QueryObject
	 *  That's since we want to be able to build queries like: <p> Items(id)  or </p> <p> Items(principalid=pid, itemid=itemid) </p> 
	 */
	public synchronized final void setId(String id)
	{
		if(mIdMap!=null && mIdMap.size() == 0)
		{
			mId = id;
		}
		else
		{
			throw new RuntimeException(SFKeywords.EXCEPTION_MSG_INVALID_PARAMETER_TO_QUERY);
		}
	}
	
	
	/**
	 *  setId() and addIds() are mutually exclusive. We will throw and exception if both are called on the same QueryObject
	 *  That's since we want to be able to build queries like: <p> Items(id)  or </p> <p> Items(principalid=pid, itemid=itemid) </p> 
	 */
	public synchronized final void addIds(String key,String value)
	{
		if(mId == null)
		{
			mIdMap.put(key, value);
		}
		else
		{
			throw new RuntimeException(SFKeywords.EXCEPTION_MSG_INVALID_PARAMETER_TO_QUERY);
		}
	}
		
	public final void addActionIds(String actionid)
	{
		throw new SFToDoReminderException(SFKeywords.EXCEPTION_MSG_NOT_IMPLEMENTED);
	}
	
	public final void addSubAction(String subaction)
	{
		throw new SFToDoReminderException(SFKeywords.EXCEPTION_MSG_NOT_IMPLEMENTED);
	}
	
	public final void setBody(SFODataObject body)
	{		
		mBody = SFDefaultGsonParser.getInstance().serialize(body.getClass(), body);
	}
	
	public final void setBody(String str)
	{		
		mBody = str;
	}
	
	public final String getBody()
	{		
		return mBody;
	}
	
	public <T> void setBody(ArrayList<T> metadata) 
	{
		throw new SFToDoReminderException(SFKeywords.EXCEPTION_MSG_NOT_IMPLEMENTED);
	}

	public final void addQueryString(String key, String value) 
	{
		mQueryMap.put(key, value);
	}
	
	public final void addQueryString(String key,SFZoneService services)
	{
		mQueryMap.put(key, services.toString());
	}
	
	public final void addQueryString(String key,Boolean value)
	{
		mQueryMap.put(key, value.toString());
	}
			
	public void addQueryString(String key, SFTreeMode treeMode) 
	{
		mQueryMap.put(key, treeMode.toString());
	}

	public void addQueryString(String key, SFVRootType rootType) 
	{
		throw new SFToDoReminderException(SFKeywords.EXCEPTION_MSG_NOT_IMPLEMENTED);		
	}

	public void addQueryString(String key, ArrayList<String> ids) 
	{
		throw new SFToDoReminderException(SFKeywords.EXCEPTION_MSG_NOT_IMPLEMENTED);		
	}

	public void addQueryString(String key, Integer size) 
	{		
		throw new SFToDoReminderException(SFKeywords.EXCEPTION_MSG_NOT_IMPLEMENTED);
	}

	public void addQueryString(String key, SFUploadMethod method) 
	{		
		throw new SFToDoReminderException(SFKeywords.EXCEPTION_MSG_NOT_IMPLEMENTED);
	}

	public void addQueryString(String key, Long fileSize) 
	{
		throw new SFToDoReminderException(SFKeywords.EXCEPTION_MSG_NOT_IMPLEMENTED);		
	}

	public void addQueryString(String key, SFApiQuery<SFSearchResults> query) 
	{
		throw new SFToDoReminderException(SFKeywords.EXCEPTION_MSG_NOT_IMPLEMENTED);		
	}

	
	/**  
	 *  Cifs and SP might have this link as depending on the host:
	 *  
	 *  <p >https://szqatest2.sharefiletest.com/cifs/v3/Items(4L24TVJSEz6Ca22LWoZg41hIVgfFgqQx0GD2VoYSgXA_) </p>
	 */
	private String getRealServerFromLink()
	{
		String ret = null;
		
		if(mLink !=null)
		{
			StringBuilder sb = new StringBuilder();
			
			sb.append(mLink.getScheme());
			sb.append(mLink.getAuthority()); //Why not use getHost()? Thats coz we want the port etc if present in the link. 
			
			String path = mLink.getPath();			
			mProvider = SFProvider.getProviderTypeFromString(path);
									
			ret = sb.toString();
		}
		
		SFLog.d2("-SApiQuery", "Parsed server from link = %s", ret);
		
		return ret;
	}
	
	
	private final String buildServerURLWithProviderAndPath(String server)
	{
		StringBuilder sb = new StringBuilder();
		
		/*
		 * In case of CIF/SP connectors lets find out the provider type and the server to connect to from the given link
		 */						
		if(mLink != null)
		{
			SFProvider provider = SFProvider.getProviderTypeFromString(mLink.getPath());
			
			if(provider != SFProvider.PROVIDER_TYPE_SF)
			{
				mProvider = provider;
				return mLink.toString();
			}
		}
		
		if(!server.startsWith(SFKeywords.PREFIX_HTTPS) && !server.startsWith(SFKeywords.PREFIX_HTTP))
		{
			sb.append(SFKeywords.PREFIX_HTTPS);
		}
		
		sb.append(server); 				
		sb.append(mProvider.toString());
		sb.append(mFromEntity);
		
		//Add the single Id or multiple comma separated key=value pairs after entity and enclose within ()
		if(mId!=null)
		{
			sb.append(SFKeywords.OPEN_BRACKET);
			sb.append(mId);
			sb.append(SFKeywords.CLOSE_BRACKET);
		}		
		else if (mIdMap!=null && mIdMap.size()>0)
		{
			sb.append(SFKeywords.OPEN_BRACKET);
			
			Set<String> keyset = mIdMap.keySet();			
			boolean appendComma = keyset.size()>1?true:false;
			
			for(String key:keyset)
			{
				String value = mIdMap.get(key);				
				sb.append(key + SFKeywords.EQUALS + value + (appendComma?SFKeywords.COMMA:SFKeywords.EMPTY));
			}
			
			sb.append(SFKeywords.CLOSE_BRACKET);
		}
		
		return sb.toString();
	}
	
	/**
	 * <p>https://server/provider/version/entity(id)
	 * 
	 * <p>https://myaccount.sf-api.com/sf/v3/Items(id)
	 *
	 * <p>https://server/provider/version/entity(principalid=pid,itemid=id)
	 * 
	 * <p>https://server/provider/version/entity(id)?$expand=Children
	 * 
	 * <p>https://server/provider/version/entity?$expand=Children
	 * 
	 * <p>https://server/provider/version/entity?$expand=Children&$select=FileCount,Id,Name,Children/Id,Children/Name,Children/CreationDate
     *
     * <p>https://account.sf-api.com/sf/v3/Items(parentid)/Folder?overwrite=false&passthrough=false 
	 */
	public final String buildQueryUrlString(String server)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(buildServerURLWithProviderAndPath(server));
		
		//Add the Actions part
		if(mAction!=null && mAction.length()>0)
		{
			sb.append(SFKeywords.FWD_SLASH);
			sb.append(mAction);
		}
		
		boolean isFirst = true;
		
		//Add query key , value pairs
		if(mQueryMap!=null && mQueryMap.size()>0)
		{
			sb.append(SFKeywords.CHAR_QUERY);
			
			//char ampersAnd = SFKeywords.CHAR_AMPERSAND.charAt(0);
			
			Set<String> keyset = mQueryMap.keySet();			
						
			for(String key:keyset)
			{
				if(!isFirst)
				{
					isFirst = false;
					sb.append(SFKeywords.CHAR_AMPERSAND);
				}
				
				String value = mQueryMap.get(key);				
				//boolean prefixAmpersAnd = (key.charAt(0) == ampersAnd)?false:true;  						
				sb.append(key + SFKeywords.EQUALS + value);				
			}
						
		}
		
		String queryUrlString = sb.toString();
		
		SFLog.d2(SFKeywords.TAG,"QUERY URL String = %s",queryUrlString);
		
		return queryUrlString;
	}
	
	public final String getHttpMethod()
	{
		return mHttpMethod;
	}	
}