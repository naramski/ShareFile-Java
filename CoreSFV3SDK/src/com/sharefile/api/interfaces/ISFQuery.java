package com.sharefile.api.interfaces;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFNotAuthorizedException;
import com.sharefile.api.exceptions.SFOAuthTokenRenewException;
import com.sharefile.api.exceptions.SFOtherException;
import com.sharefile.api.exceptions.SFServerException;
import com.sharefile.api.models.SFODataObject;

public interface ISFQuery<T> extends ISFTypeFilter
{
    ISFQuery<T>  setApiClient(ISFApiClient apiClient);

    ISFQuery<T>  setFrom(String string);

    ISFQuery<T>  setHttpMethod(String string);

    ISFQuery<T>  addIds(URI url);

    ISFQuery<T>  setAction(String string);

    ISFQuery<T>  setBody(SFODataObject sfoDataObject);

    ISFQuery<T>   setBody(ArrayList<?> sfoDataObjectsFeed);

    ISFQuery<T>  addQueryString(String string, Object type);

    ISFQuery<T>  addActionIds(String id);

    ISFQuery<T>  addQueryString(String string, ArrayList<String> ids);

    ISFQuery<T>  addSubAction(String string);

	URI getLink();

	String getUserName();

	String getPassword();

	String getHttpMethod();

	String getBody();

	boolean constructDownloadSpec();

	String buildQueryUrlString(String server) throws UnsupportedEncodingException;

    ISFQuery<T> setLink(String string) throws URISyntaxException;
	
	/**
	 *  This implies that the query parameters need to be appended by the buildQuery
     *  function before executing the query.
	 */
    ISFQuery<T> setLink(URI uri);
	
	/**
	 * This implies that the query parameters are included in the URI and no more parameters more
     * needs to be added before executing the query.
	 * Generally we get such URI from Redirection object.
	 */
    ISFQuery<T>  setFullyParametrizedLink(URI uri);

	boolean canReNewTokenInternally();

	ISFQuery<T> setCredentials(String userName, String password);
	
	/**
	 *  For certain calls like create symbolic link we want to disable readahead done by the
     *  SDK. This function allows to set the flag to explicity false if required..
	 */
    ISFQuery<T> allowRedirection(boolean value);

	boolean reDirectionAllowed();
	
	/**
	 * This will append the query paremeters from previuos query to the new link. use this only
     * when re-executing the query for a redirected object.
	 * Also , this will ignore the previous params if new query already has some params
	 * @throws URISyntaxException 
	 * @throws UnsupportedEncodingException 
	 */
    ISFQuery<T>  setLinkAndAppendPreviousParameters(URI uri) throws URISyntaxException, UnsupportedEncodingException;

	/**
	 * This will append the query paremeters from previuos query to the new link. use this only
     * when re-executing the query for a redirected object.
	 * Also , this will ignore the previous params if new query already has some params
	 * @throws URISyntaxException 
	 * @throws UnsupportedEncodingException 
	 */
    ISFQuery<T>  setLinkAndAppendPreviousParameters(String string) throws URISyntaxException, UnsupportedEncodingException;;

    /**
     * simplifies the adding of expansion parameters to the query.
    */
    ISFQuery<T> expand(String expansionParameter);


    /**
     This function takes any uri and stores only its base part along with the provider

     example if you pass: https://szqatest2.sharefiletest.com/cifs/v3/Capabilities

     this function will store baseLink as : https://szqatest2.sharefiletest.com/cifs/v3/
     */
    ISFQuery<T>  setBaseLink(URI uri) throws URISyntaxException;

    public T execute() throws SFInvalidStateException, SFServerException,
            SFNotAuthorizedException,SFOAuthTokenRenewException, SFOtherException;

    void executeAsync(ISFApiResultCallback<T> callback) throws SFInvalidStateException;
}