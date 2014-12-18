package com.sharefile.api;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFQueryParams;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.enumerations.SFProvider;
import com.sharefile.api.enumerations.SFSafeEnum;
import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.exceptions.SFToDoReminderException;
import com.sharefile.api.gson.auto.SFDefaultGsonParser;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFSearchResults;
import com.sharefile.api.models.SFTreeMode;
import com.sharefile.api.models.SFUploadMethod;
import com.sharefile.api.models.SFVRootType;
import com.sharefile.api.models.SFZoneService;
import com.sharefile.api.utils.Utils;
import com.sharefile.java.log.SLog;

public class SFApiQuery<T> implements ISFQuery<T>
{
	private static final String TAG = "SFApiQuery";
	
	/**
	 * https://server/provider/version/entity(id)
	 * 
	 * https://myaccount.sf-api.com/sf/v3/Items(id)
	 *
	 */
	private String mFromEntity = null;
	private String mAction = null;
	private String mSubAction = null;
	private String mHttpMethod = null;
	private SFProvider mProvider = SFProvider.PROVIDER_TYPE_SF;
	private String mId = null;
	private String mActionId = null;
	private Map<String,String> mQueryMap = new HashMap<String, String>();
	private Map<String,String> mIdMap = new HashMap<String, String>();	
	private String mBody = null;
	private URI mLink = null; //The URL link obtained for V3connectors from their symbolic link or 302 redirect.
	private boolean mLinkIsParametrized = false;

    /**
     The client has an option to add query any parameters as follows:

     ArrayList<String> expand = new ArrayList<String>(){};
     expand.add(SFKeywords.INFO);
     expand.add(SFKeywords.CHILDREN);
     expand.add(SFKeywords.REDIRECTION);
     expand.add(SFKeywords.CHILDREN+ "/" +SFKeywords.PARENT);
     expand.add(SFKeywords.CHILDREN+ "/" +SFKeywords.REDIRECTION);
     addQueryString(SFQueryParams.EXPAND, expand);

     Expansion parameters are most frequently used so provide a simpler way
     for the client to add them. so that the client can call query.expand("somevalue1").expand("somevalue2")....expand("somevaluen") etc
     */
    private final ArrayList<String> mExpansionParameters = new ArrayList<String>(){};
    private final SFFilterParam mFilter = new SFFilterParam();

	/** 
	 * Currently the server is not returning a DownloadSpecification for download requests, 
	 * its directly returning the download link. For the sake of completeness, implement the local
	 * response filler for such requests.	 
	 */			
	public boolean constructDownloadSpec()
	{
		boolean ret = false;
		
		if(SFKeywords.Items.equalsIgnoreCase(mFromEntity) && SFKeywords.Download.equalsIgnoreCase(mAction))
		{
			ret = true;
		}
			
		return ret;
	}
	
	/**
	 * The username and password are used only for connectors auth. These can be set during auth errors or explicitly set during 
	 * the very first call from this query to avoid double round-trips to the server. We let the application handle setting of this
	 * TODO: For security purpose we may want to wipe the credentials from this object when done using for auth.
	 */
	private String mUserName;
	
	/**
	 * The username and password are used only for connectors auth. These can be set during auth errors or explicitly set during 
	 * the very first call from this query to avoid double round-trips to the server. We let the application handle setting of this
	 * TODO: For security purpose we may want to wipe the credentials from this object when done using for auth.
	 */
	private String mPassword;

	private boolean mEnableRedirection = true;
	
	//{@link #getComponentAt(int, int) getComponentAt} method.
	
	/**
	 * When whenever you want to re-execute a previous query with slightly different parameters
	 * always use this function to copy feilds from the source query and then modify the necessry feilds.
	 */
	public void copyQuery(SFApiQuery<T> sourceQuery)
	{
		mFromEntity = sourceQuery.mFromEntity;
		mAction = sourceQuery.mAction;
		mHttpMethod = sourceQuery.mHttpMethod;
		mProvider = sourceQuery.mProvider;
		mId = sourceQuery.mId;
		mQueryMap.putAll(sourceQuery.mQueryMap);
		mIdMap.putAll(sourceQuery.mIdMap);
		mBody = sourceQuery.mBody;
		mLink = sourceQuery.mLink;
		mUserName = sourceQuery.mUserName;
		mPassword = sourceQuery.mPassword;
	}
				
	public void setCredentials(final String userName,final String password)
	{
		mUserName = userName;
		mPassword = password;
	}
	
	public final String getUserName()
	{
		return mUserName;		
	}
	
	public final String getPassword()
	{
		return mPassword;		
	}
	
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
	
	public final URI getLink()
	{
		return mLink;
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
		mActionId = actionid;
	}
	
	public final void addActionIds(SFSafeEnum actionId)
	{
		mActionId = actionId.getOriginalString();
	}
	
	public final void addSubAction(String subaction)
	{
		mSubAction = subaction;
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

    public final void addQueryString(String key, String value)
    {
        if(Utils.isEmpty(key))
        {
            return;
        }

        //put expansion parameters in expansion map instead
        if(SFQueryParams.EXPAND.equals(key))
        {
            expand(value);
            return;
        }

        mQueryMap.put(key, value);
    }

	public void addQueryString(String key, ArrayList<String> ids) 
	{
		if(ids == null || key == null)
        {
            return;
        }

        //put expansion parameters in expansion map instead
        if(SFQueryParams.EXPAND.equals(key))
        {
            expand(ids);
            return;
        }

        addQueryStringInternal(key,ids);
	}

    private void addQueryStringInternal(String key, ArrayList<String> ids)
    {
        if(ids!=null)
        {
            StringBuilder sb = new StringBuilder();

            boolean isFirst = true;

            for(String str:ids)
            {
                if(!isFirst)
                {
                    sb.append(SFKeywords.COMMA);
                }
                else
                {
                    isFirst = false;
                }

                sb.append(str);
            }

            mQueryMap.put(key, sb.toString());
        }
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

	@Deprecated
	public void addQueryString(String key, SFApiQuery<SFSearchResults> query) 
	{
		throw new SFToDoReminderException(SFKeywords.EXCEPTION_MSG_NOT_IMPLEMENTED);		
	}

    private boolean isBaseLink(URI uri)
    {
        String path = uri.getPath();

        if(path !=null && path.replaceAll("/","").length()>0)
        {
           return false;
        }

        return true;
    };

    /**
       This functions builds the query url part with :

       https://subdomain.domain.com/provider/FromEntity(ids,..)

     */
	private final String buildServerURLWithProviderAndEntity(String server)
	{
		StringBuilder sb = new StringBuilder();
		
		/*
		 * In case of CIF/SP connectors lets find out the provider type and the server to connect to from the given link
		 */						
		if(mLink != null)
		{
            if(!isBaseLink(mLink))
            {
                SFProvider provider = SFProvider.getProviderType(mLink.getPath());

                mProvider = provider;
                return mLink.toString();
            }

            server = mLink.toString();
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
		if(mLinkIsParametrized && mLink!=null)
		{
			SLog.d(TAG,"Link is fully parametrized");
			return mLink.toString();
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(buildServerURLWithProviderAndEntity(server));
		//Add the Actions part
		if(!Utils.isEmpty(mAction))
		{
			sb.append(SFKeywords.FWD_SLASH);
			sb.append(mAction);
			
			//Add action id
			if(!Utils.isEmpty(mActionId))
			{
				sb.append(SFKeywords.OPEN_BRACKET);
				sb.append(mActionId);
				sb.append(SFKeywords.CLOSE_BRACKET);
			}
			
			//Add sub action			
			if(!Utils.isEmpty(mSubAction))
			{
				sb.append(SFKeywords.FWD_SLASH);
				sb.append(mSubAction);
			}
		}
		
		String queryParams = buildQueryParameters();
		
		if(!Utils.isEmpty(queryParams))
		{
			sb.append(SFKeywords.CHAR_QUERY);
			sb.append(queryParams);
		}
				
		String queryUrlString = sb.toString();
		
		SLog.d(SFKeywords.TAG,"QUERY URL String = " + queryUrlString);
		
		return queryUrlString;
	}


    private void addExpansionParams()
    {
        if(mExpansionParameters.size()>0)
        {
            addQueryStringInternal(SFQueryParams.EXPAND, mExpansionParameters);
        }
    }

    private void addFilterParams()
    {
        String filters = mFilter.get();

        if(!Utils.isEmpty(filters))
        {
            addQueryString(SFQueryParams.FILTER, filters);
        }
    }

    private void addAllQueryParams()
    {
        addExpansionParams();
        addFilterParams();
    }

	private String buildQueryParameters() throws UnsupportedEncodingException
	{
        addAllQueryParams();

		StringBuilder sb = new StringBuilder();
						
		boolean isFirst = true;
		
		//Add query key , value pairs
		if(mQueryMap!=null && mQueryMap.size()>0)
		{						
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

					String urlencoded = URLEncoder.encode(value, SFKeywords.UTF_8).replace("+", "%20");

					sb.append(key + SFKeywords.EQUALS + urlencoded);
				}
			}						
		}
		
		return sb.toString();
	}
	
	public final String getHttpMethod()
	{
		return mHttpMethod;
	}
	
	/**
	 * we can renew token for Sharefile providers. 
	 */
	public boolean canReNewTokenInternally()
	{				
		boolean ret = true;
		
		if(mLink!=null)
		{
			ret = (SFProvider.PROVIDER_TYPE_SF == SFProvider.getProviderType(mLink)?true:false);
		}
		
		return ret;
	}

	@Override
	public void setHttpMethod(String string) 
	{
		mHttpMethod = string;
	}

	@Override
	public void addIds(URI url) 
	{		
		mLink = url;		
	}

	@Override
	public void addQueryString(String key, SFSafeEnum value) 
	{
		mQueryMap.put(key, value.toString());
	}

	@Override
	public void addQueryString(String key, Date date) 
	{		
		mQueryMap.put(key, date.toString());
	}

	@Override
	public void setBody( ArrayList<?> sfoDataObjectsFeed) 
	{
        SLog.e(TAG,"This is not implemented");
	}

	@Override
	public void setLink(URI uri) 
	{
		mLinkIsParametrized = false;
		mLink = uri;
	}

	@Override
	public void setFullyParametrizedLink(URI uri) 
	{
		mLinkIsParametrized = true;
		mLink = uri;
	}

	@Override
	public void setRedirection(boolean value)
	{
        mEnableRedirection = value;
	}
	
	public boolean reDirectionAllowed()
	{
		return mEnableRedirection;
	}

	@Override
	public void setLinkAndAppendPreviousParameters(URI newuri) throws URISyntaxException, UnsupportedEncodingException 
	{	
		String newQueryParams = newuri.getQuery(); 
		
		if(newQueryParams !=null)
		{
			setFullyParametrizedLink(newuri);
			return;
		}
		
		String oldQueryParms = buildQueryParameters();		
		
		StringBuilder sb = new StringBuilder();
		sb.append(newuri.toString());
		
		if(!Utils.isEmpty(oldQueryParms))
		{
			sb.append(SFKeywords.CHAR_QUERY);
			sb.append(oldQueryParms);
		}
		
		String strNewUrl = sb.toString();
		
		SLog.d(TAG,"Setting new URL by appending old query parameter to: " + strNewUrl);
		
		setFullyParametrizedLink(new URI(strNewUrl));		
	}

	@Override
	public void setLinkAndAppendPreviousParameters(String string) throws URISyntaxException, UnsupportedEncodingException 
	{
		setLinkAndAppendPreviousParameters(new URI(string));		
	}

    @Override
    public ISFQuery<T> expand(String expansionParameter)
    {
       if(Utils.isEmpty(expansionParameter))
       {
           return this;
       }

       mExpansionParameters.add(expansionParameter);

       return this;
    }

    @Override
    public ISFQuery<T> filter(String filterValue)
    {
        if(Utils.isEmpty(filterValue))
        {
            return this;
        }

        mFilter.filter(filterValue);

        return this;
    }

    @Override
    public ISFQuery and(SFV3ElementType type)
    {
        mFilter.and(type);
        return this;
    }

    @Override
    public ISFQuery or(SFV3ElementType type)
    {
        mFilter.or(type);
        return this;
    }

    @Override
    public ISFQuery is(SFV3ElementType type)
    {
        mFilter.is(type);
        return this;
    }

    private void expand(ArrayList<String> expansionParameters)
    {
        if(Utils.isEmpty(expansionParameters))
        {
            return ;
        }

        for(String str: expansionParameters)
        {
            mExpansionParameters.add(str);
        }
    }

    /**
     This function takes any uri and stores only its base part along with the provider

     example if you pass: https://szqatest2.sharefiletest.com/cifs/v3/Capabilities

     This function will store baseLink as : https://szqatest2.sharefiletest.com
     */
    @Override
    public void setBaseLink(URI uri) throws URISyntaxException
    {
        mProvider = SFProvider.getProviderType(uri);

        String host = uri.getHost();
        String path = uri.getPath();
        String protocol = uri.getScheme();

        mLink = new URI(protocol + "://" + host);
    }
}