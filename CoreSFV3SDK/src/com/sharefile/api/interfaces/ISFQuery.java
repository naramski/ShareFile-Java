package com.sharefile.api.interfaces;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.sharefile.api.SFApiResultCallbackEx;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFNotAuthorizedException;
import com.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.sharefile.api.exceptions.SFV3ErrorException;
import com.sharefile.api.models.SFODataObject;

public interface ISFQuery<T> extends ISFTypeFilter
{
	void setFrom(String string);

	void setHttpMethod(String string);

	void addIds(URI url);

	void setAction(String string);

	void setBody(SFODataObject sfoDataObject);
	
	void  setBody(ArrayList<?> sfoDataObjectsFeed);

    void addQueryString(String string, Object type);

	void addActionIds(String id);

	void addQueryString(String string, ArrayList<String> ids);

	void addSubAction(String string);

	URI getLink();

	String getUserName();

	String getPassword();

	String getHttpMethod();

	String getBody();

	boolean constructDownloadSpec();

	String buildQueryUrlString(String server) throws UnsupportedEncodingException;

	void setLink(String string) throws URISyntaxException;
	
	/**
	 *  This implies that the query parameters need to be appended by the buildQuery function before executing the query.
	 */
	void setLink(URI uri);
	
	/**
	 * This implies that the query parameters are included in the URI and no more parameters more needs to be added before executing the query.
	 * Generally we get such URI from Redirection object.
	 */
	void setFullyParametrizedLink(URI uri);

	boolean canReNewTokenInternally();

	void setCredentials(String userName, String password);
	
	/**
	 *  For certain calls like create symbolic link we want to disable readahead done by the SDK. This function allows to set the flag to explicity false if required..
	 */
	void setRedirection(boolean value);

	boolean reDirectionAllowed();
	
	/**
	 * This will append the query paremeters from previuos query to the new link. use this only when re-executing the query for a redirected object.
	 * Also , this will ignore the previous params if new query already has some params
	 * @throws URISyntaxException 
	 * @throws UnsupportedEncodingException 
	 */
	void setLinkAndAppendPreviousParameters(URI uri) throws URISyntaxException, UnsupportedEncodingException;

	/**
	 * This will append the query paremeters from previuos query to the new link. use this only when re-executing the query for a redirected object.
	 * Also , this will ignore the previous params if new query already has some params
	 * @throws URISyntaxException 
	 * @throws UnsupportedEncodingException 
	 */
	void setLinkAndAppendPreviousParameters(String string) throws URISyntaxException, UnsupportedEncodingException;;

    /**
     * simplifies the adding of expansion parameters to the query.
    */
    ISFQuery<T> expand(String expansionParameter);


    /**
     This function takes any uri and stores only its base part along with the provider

     example if you pass: https://szqatest2.sharefiletest.com/cifs/v3/Capabilities

     this function will store baseLink as : https://szqatest2.sharefiletest.com/cifs/v3/
     */
    void setBaseLink(URI uri) throws URISyntaxException;

    public T execute() throws SFInvalidStateException, SFV3ErrorException, SFNotAuthorizedException, SFOAuthTokenRenewException;

    void executeAsync(ISFApiResultCallback<T> callback) throws SFInvalidStateException;
}