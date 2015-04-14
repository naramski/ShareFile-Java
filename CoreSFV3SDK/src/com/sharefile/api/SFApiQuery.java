package com.sharefile.api;

import com.sharefile.api.async.SFAsyncHelper;
import com.sharefile.api.async.SFAsyncTaskFactory;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFQueryParams;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.enumerations.SFSafeEnum;
import com.sharefile.api.enumerations.SFV3ElementType;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFNotAuthorizedException;
import com.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.sharefile.api.exceptions.SFOtherException;
import com.sharefile.api.exceptions.SFToDoReminderException;
import com.sharefile.api.exceptions.SFServerException;
import com.sharefile.api.gson.auto.SFDefaultGsonParser;
import com.sharefile.api.interfaces.ISFApiClient;
import com.sharefile.api.interfaces.ISFApiResultCallback;
import com.sharefile.api.interfaces.ISFAsyncTask;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFQuery;
import com.sharefile.api.models.SFSearchResults;
import com.sharefile.api.utils.Utils;
import com.sharefile.api.log.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SFApiQuery<T extends SFODataObject> implements ISFQuery<T>
{
	private static final String TAG = "SFApiQuery";
    private ISFApiClient apiClient;
	
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
	private String mProviderForUrlPath = "/"+SFProvider.PROVIDER_TYPE_SF+"/v3/";
	private String mId = null;
	private String mActionId = null;
	private final Map<String,String> mQueryMap = new HashMap<String, String>();
	private final Map<String,String> mIdMap = new HashMap<String, String>();
	private String mBody = null;
	private URI mLink = null; //The URL link obtained for V3connectors from their symbolic link or 302 redirect.
	private boolean mLinkIsParametrized = false;

    @Override
    public ISFQuery<T> setApiClient(ISFApiClient apiClient)
    {
        this.apiClient = apiClient;
        return this;
    }

    /**
     The apiClient has an option to add query any parameters as follows:

     ArrayList<String> expand = new ArrayList<String>(){};
     expand.add(SFKeywords.INFO);
     expand.add(SFKeywords.CHILDREN);
     expand.add(SFKeywords.REDIRECTION);
     expand.add(SFKeywords.CHILDREN+ "/" +SFKeywords.PARENT);
     expand.add(SFKeywords.CHILDREN+ "/" +SFKeywords.REDIRECTION);
     addQueryString(SFQueryParams.EXPAND, expand);

     Expansion parameters are most frequently used so provide a simpler way
     for the apiClient to add them. so that the apiClient can call query.expand("somevalue1").expand("somevalue2")....expand("somevaluen") etc
     */
    private final ArrayList<String> mExpansionParameters = new ArrayList<String>(){};
    private final SFFilterParam mFilter = new SFFilterParam();

    public SFApiQuery(ISFApiClient client)
    {
        this.apiClient = client;
    }



    /**
	 * Currently the server is not returning a DownloadSpecification for download requests, 
	 * its directly returning the download link. For the sake of completeness, implement the local
	 * response filler for such requests.	 
	 */
    @Override
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

	private boolean allowRedirection = true;
	
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
		mProviderForUrlPath = sourceQuery.mProviderForUrlPath;
		mId = sourceQuery.mId;
		mQueryMap.putAll(sourceQuery.mQueryMap);
		mIdMap.putAll(sourceQuery.mIdMap);
		mBody = sourceQuery.mBody;
		mLink = sourceQuery.mLink;
		mUserName = sourceQuery.mUserName;
		mPassword = sourceQuery.mPassword;
	}

    @Override
	public ISFQuery<T> setCredentials(final String userName,final String password)
	{
		mUserName = userName;
		mPassword = password;
        return this;
	}

    @Override
	public final String getUserName()
	{
		return mUserName;		
	}

    @Override
	public final String getPassword()
	{
		return mPassword;		
	}

    @Override
	public final ISFQuery<T>  setFrom(String setFrom)
	{
		mFromEntity = setFrom;
        return this;
	}

    /**
        This function takes any uri and store it entirely.
        example if you pass: https://szqatest2.sharefiletest.com/cifs/v3/Items(randdomid)
        This function will store it as: https://szqatest2.sharefiletest.com/cifs/v3/Items(randdomid)
        if the query needs additional params, the call to buildQueryUrlString() will add those to this
        one. In case you want to avoid that, call setFullyParametrizedLink() instead.
    */
    @Override
	public final ISFQuery<T>  setLink(String link) throws URISyntaxException
	{
		if(link!=null)
		{
			mLink = new URI(link);
		}
        return this;
	}

    @Override
	public final URI getLink()
	{
		return mLink;
	}
	
	public final void setProvider(String provider)
	{
		mProviderForUrlPath = provider;
	}

    @Override
	public final ISFQuery<T>  setAction(String action)
	{
		mAction = action;
        return this;
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

    @Override
	public final ISFQuery<T>  addActionIds(String actionid)
	{
		mActionId = actionid;
        return this;
	}
	
	public final ISFQuery<T>  addActionIds(SFSafeEnum actionId)
	{
		mActionId = actionId.getOriginalString();
        return this;
	}

    @Override
	public final ISFQuery<T>  addSubAction(String subaction)
	{
		mSubAction = subaction;
        return this;
	}

    @Override
	public final ISFQuery<T>  setBody(SFODataObject body)
	{				
		mBody = SFDefaultGsonParser.serialize(body.getClass(), body);
        return this;
	}

	public final ISFQuery<T>  setBody(String str)
	{		
		mBody = str;
        return this;
	}

    @Override
	public final String getBody()
	{		
		return mBody;
	}

    @Override
	public final ISFQuery<T>  addQueryString(String key,Object object)
	{
        if(object == null || key == null)
        {
            Logger.d(TAG,"Cannot add NULL parameter to queryString");
            return this;
        }

		mQueryMap.put(key, object.toString());
        return this;
	}

    @Override
	public ISFQuery<T>  addQueryString(String key, ArrayList<String> ids)
	{
		if(ids == null || key == null)
        {
            return this;
        }

        //put expansion parameters in expansion map instead
        if(SFQueryParams.EXPAND.equals(key))
        {
            expand(ids);
            return this;
        }

        addQueryStringInternal(key,ids);
        return this;
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
                mProviderForUrlPath = "/"+SFProvider.getProviderType(mLink.getPath())+"/v3/";

                return mLink.toString();
            }

            server = mLink.toString();
		}

		if(!server.startsWith(SFKeywords.PREFIX_HTTPS) && !server.startsWith(SFKeywords.PREFIX_HTTP))
		{
			sb.append(SFKeywords.PREFIX_HTTPS);
		}
		
		sb.append(server); 				
		sb.append(mProviderForUrlPath);
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
			boolean appendComma = keyset.size() > 1;
			
			for(String key:keyset)
			{
				String value = mIdMap.get(key);				
				sb.append(key);
                sb.append(SFKeywords.EQUALS);
                sb.append(value);
                if(appendComma)
                {
                    sb.append(SFKeywords.COMMA);
                }
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
    @Override
	public final String buildQueryUrlString(String server) throws UnsupportedEncodingException
	{
		if(mLinkIsParametrized && mLink!=null)
		{
			Logger.d(TAG,"Link is fully parametrized");
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
		
		Logger.d(SFKeywords.TAG,"QUERY URL String = " + queryUrlString);
		
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

					sb.append(key);
                    sb.append(SFKeywords.EQUALS);
                    sb.append(urlencoded);
				}
			}						
		}
		
		return sb.toString();
	}

    @Override
	public final String getHttpMethod()
	{
		return mHttpMethod;
	}
	
	/**
	 * we can renew token for Sharefile providers. 
	 */
    @Override
	public boolean canReNewTokenInternally()
	{				
		boolean ret = true;
		
		if(mLink!=null)
		{
			ret = (SFProvider.PROVIDER_TYPE_SF == SFProvider.getProviderType(mLink));
		}
		
		return ret;
	}

	@Override
	public ISFQuery<T>  setHttpMethod(String string)
	{
		mHttpMethod = string;
        return this;
	}

	@Override
	public ISFQuery<T>  addIds(URI url)
	{		
		mLink = url;
        return this;
	}

	@Override
	public ISFQuery<T>  setBody( ArrayList<?> sfoDataObjectsFeed)
	{
        Logger.e(TAG,"This is not implemented");
        return this;
	}

	@Override
	public ISFQuery<T>  setLink(URI uri)
	{
		mLinkIsParametrized = false;
		mLink = uri;
        return this;
	}

	@Override
	public ISFQuery<T>  setFullyParametrizedLink(URI uri)
	{
		mLinkIsParametrized = true;
		mLink = uri;
        return this;
	}

	@Override
	public ISFQuery<T> allowRedirection(boolean value)
	{
        allowRedirection = value;
        return this;
	}

    @Override
	public boolean reDirectionAllowed()
	{
		return allowRedirection;
	}

	@Override
	public ISFQuery<T> setLinkAndAppendPreviousParameters(URI newuri) throws URISyntaxException, UnsupportedEncodingException
	{	
		String newQueryParams = newuri.getQuery();
        String oldQueryParms = buildQueryParameters();
		
		if(newQueryParams !=null && newQueryParams.contains(oldQueryParms))
		{
			setFullyParametrizedLink(newuri);
			return this;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(newuri.toString());
		
		if(!Utils.isEmpty(oldQueryParms))
		{
            if(Utils.isEmpty(newQueryParams))
            {
                sb.append(SFKeywords.CHAR_QUERY);
            }
            else
            {
                sb.append(SFKeywords.CHAR_AMPERSAND);
            }

			sb.append(oldQueryParms);
		}
		
		String strNewUrl = sb.toString();
		
		Logger.d(TAG,"Setting new URL by appending old query parameter to: " + strNewUrl);
		
		setFullyParametrizedLink(new URI(strNewUrl));

        return this;
	}

	@Override
	public ISFQuery<T>  setLinkAndAppendPreviousParameters(String string) throws URISyntaxException, UnsupportedEncodingException
	{
		setLinkAndAppendPreviousParameters(new URI(string));
        return this;
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
    public ISFQuery<T> top(int topItems)
    {
        addQueryString(SFQueryParams.TOP,topItems);
        return this;
    }

    @Override
    public ISFQuery<T> skip(int skipItems)
    {
        addQueryString(SFQueryParams.SKIP,skipItems);
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
    public ISFQuery<T>  setBaseLink(URI uri) throws URISyntaxException
    {
        mProviderForUrlPath = "/"+SFProvider.getProviderType(uri)+"/v3/";

        String host = uri.getHost();
        String protocol = uri.getScheme();

        mLink = new URI(protocol + "://" + host);
        return this;
    }

    @Override
    public T execute() throws SFInvalidStateException, SFServerException,
            SFNotAuthorizedException, SFOAuthTokenRenewException, SFOtherException
    {

        if(apiClient==null)
        {
            throw new SFInvalidStateException("No valid client object set for query");
        }

        return (T)apiClient.executeQuery(this);
    }

    @Override
    public void executeAsync(ISFApiResultCallback<T> callback) throws
            SFInvalidStateException
    {
        if(apiClient==null)
        {
            throw new SFInvalidStateException("No valid client object set for query");
        }

        if(callback == null)
        {
            throw new SFInvalidStateException("Need to set listener to gather Async Result");
        }

        SFAsyncHelper asyncHelper = new SFAsyncHelper(apiClient, this, callback);

        ISFAsyncTask asyncTask = SFAsyncTaskFactory.create();

        if(asyncTask == null)
        {
            throw new SFInvalidStateException("Need to set AsyncFactory as per your system");
        }

        asyncTask.start(asyncHelper);
    }
}