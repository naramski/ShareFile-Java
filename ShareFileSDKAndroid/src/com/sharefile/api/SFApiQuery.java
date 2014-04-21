package com.sharefile.api;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.enumerations.SFProvider;
import com.sharefile.api.exceptions.SFToDoReminderException;
import com.sharefile.api.gson.auto.SFDefaultGsonParser;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSearchResults;
import com.sharefile.api.models.SFTreeMode;
import com.sharefile.api.models.SFUploadMethod;
import com.sharefile.api.models.SFVRootType;
import com.sharefile.api.models.SFZoneService;
import com.sharefile.api.utils.SFLog;

public class SFApiQuery<T extends SFODataObject> 
{
	
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
	private String mId = null;
	private Map<String,String> mQueryMap = new HashMap<String, String>();
	private Map<String,String> mIdMap = new HashMap<String, String>();	
	private String mBody = null;
	private URI mLink = null; //The URL link obtained for V3connectors from their symbolic link or 302 redirect.
		
	public final void setFrom(String setFrom)
	{
		mFromEntity = setFrom;								
	}
	
	public final void setLink(String link) throws URISyntaxException
	{
		if(link!=null)
		{
			mLink = new URI(link);
		}
	}
	
	public final void setProvider(SFProvider provider)
	{
		mProvider = provider;
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
		mBody = SFDefaultGsonParser.serialize(body.getClass(), body);
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
		mQueryMap.put(key, rootType.toString());		
	}

	public void addQueryString(String key, ArrayList<String> ids) 
	{
		throw new SFToDoReminderException(SFKeywords.EXCEPTION_MSG_NOT_IMPLEMENTED);		
	}

	public void addQueryString(String key, Integer size) 
	{		
		mQueryMap.put(key, ""+size);
	}

	public void addQueryString(String key, SFUploadMethod method) 
	{		
		mQueryMap.put(key, method.toString());
	}

	public void addQueryString(String key, Long fileSize) 
	{
		mQueryMap.put(key, ""+fileSize);		
	}

	public void addQueryString(String key, SFApiQuery<SFSearchResults> query) 
	{
		throw new SFToDoReminderException(SFKeywords.EXCEPTION_MSG_NOT_IMPLEMENTED);		
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
	 * @throws UnsupportedEncodingException 
	 */
	public final String buildQueryUrlString(String server) throws UnsupportedEncodingException
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
				String value = mQueryMap.get(key);
				
				if(value!=null)
				{
					if(!isFirst)
					{					
						sb.append(SFKeywords.CHAR_AMPERSAND);
					}
					else
					{
						isFirst = false;	
					}
					
					String urlencoded = URLEncoder.encode(value, SFKeywords.UTF_8);
					sb.append(key + SFKeywords.EQUALS + urlencoded);
				}
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
	
	/**
	 * *If the query does not involve a connector we can try to internally handle the renewal of oAuth token.
	 */
	public boolean canhandleReAuthInternally()
	{
		return (mLink == null)? true:false;
	}
}