package com.sharefile.api.interfaces;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;

import com.sharefile.api.enumerations.SFSafeEnum;
import com.sharefile.api.models.SFODataObject;

public interface ISFQuery<T> 
{

	void setFrom(String string);

	void setHttpMethod(String string);

	void addIds(URI url);

	void setAction(String string);

	void addQueryString(String string, Boolean recursive);

	void setBody(SFODataObject sfoDataObject);
	
	<T extends SFODataObject > void  setBody(ArrayList<T> sfoDataObjectsFeed);

	void addQueryString(String string, String type);

	void addActionIds(String id);

	void addQueryString(String string, SFSafeEnum value);

	void addQueryString(String string, Long value);

	void addQueryString(String string, ArrayList<String> ids);

	void addQueryString(String string, Integer size);

	void addQueryString(String string, Date date);

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
}