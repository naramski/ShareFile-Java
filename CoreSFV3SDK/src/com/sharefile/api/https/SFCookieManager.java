package com.sharefile.api.https;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import com.sharefile.api.SFSDKDefaultAccessScope;
import com.sharefile.java.log.SLog;

public class SFCookieManager 
{
	private static final String TAG = "SFCookieManager"; 
	
	/**
	 *   mStore is the map of all domains and their corresponding cookie stores.	    
	 *   so if we have cookies from hosts like:	 citrix.sharefile.com   or  domian.host.com
	 *   this map will store the cookies stores like:  
	 *   <br>(citrix, map(cookies) )
	 *   <br>(domain, map(cookies) ) ...  
	 *   
	 *   
	 *   <br>The inner map of this store is a map of cookie names vs the cookie field name,value pairs
	 *   
	 *   <br>So the entire map structure becomes:
	 *   
	 *     <p>mStore
	 *     <br>|
	 *     <br>--> Map (domain1, DomainStore2)
	 *     <br>|  
	 *     <br>--> Map (domain2, DomainStore2)
	 *     	 
	 *     <p>DomainStore
	 *     <br>|
	 *     <br>--> Map(cookieName1, Cookie1)
	 *     <br>|
	 *     <br>--> Map(cookieName2, Cookie2)
	 *     
	 *     <p>Cookie
	 *     <br>|
	 *     <br>--> Map(cookieFeild1, value)
	 *     <br>|
	 *     <br>--> Map(cookieFeild2, value)
	 */
    private Map<String,Map<String,Map<String,String>>> mStore;

    private static final String SET_COOKIE = "Set-Cookie";
    private static final String COOKIE_VALUE_DELIMITER = ";";
    private static final String PATH = "path";
    private static final String EXPIRES = "expires";
    private static final String DATE_FORMAT = "EEE, dd-MMM-yyyy hh:mm:ss z";
    private static final String SET_COOKIE_SEPARATOR="; ";
    private static final String COOKIE = "Cookie";

    private static final char NAME_VALUE_SEPARATOR = '=';
    private static final char DOT = '.';
    
    private DateFormat dateFormat;

    public SFCookieManager() 
    {
		mStore = new HashMap<String,Map<String, Map<String, String>>>();
		dateFormat = new SimpleDateFormat(DATE_FORMAT,Locale.US);
    }
    
    public synchronized void clearAllCookies()
    {
    	if(mStore!=null)
    	{
    		mStore.clear();
    	}
    }
    
    /**
     *  Allows the app to store cookies inside the cookie store so that it will automatically be set for connections to the 
     *  specified domain
     */
    public void storeAppSpecificCookies(URI uri,String cookieString)
    {
    	Map<String,Map<String,String>> domainStore = getDomainStoreFromHost(uri.getHost());
    	
    	storeCookieToDomainStore(domainStore, cookieString);
    }
     
    /**
     *  Allows the app to store cookies inside the cookie store so that it will automatically be set for connections to the 
     *  specified domain
     */
    public void storeAppSpecificCookies(String urlstr,String cookieString)
    {    	
		try 
		{
			URI uri = new URI(urlstr);
			storeAppSpecificCookies(uri, cookieString);
		} 
		catch (URISyntaxException e) 
		{			
			SLog.e(TAG,e);
		}    	    	
    }
            
    private Map<String,Map<String,String>> getDomainStoreFromHost(String host)
    {
    	// let's determine the domain from where these cookies are being sent
		String domain = getDomainFromHost(host);
				
		Map<String,Map<String,String>> domainStore; // this is where we will store cookies for this domain
		
		// now let's check the store to see if we have an entry for this domain
		if (mStore.containsKey(domain)) 
		{
		    // we do, so lets retrieve it from the store
		    domainStore = mStore.get(domain);
		} 
		else 
		{
		    // we don't, so let's create it and put it in the store
		    domainStore = new HashMap<String, Map<String, String>>();
		    mStore.put(domain, domainStore);    
		}
		
		return domainStore;
    }
    
    private void storeCookieToDomainStore(Map<String,Map<String,String>> domainStore,String cookieString)
    {
    	Map<String,String> cookie = new HashMap<String,String>();
		StringTokenizer st = new StringTokenizer(cookieString, COOKIE_VALUE_DELIMITER);
		
		// the specification dictates that the first name/value pair
		// in the string is the cookie name and value, so let's handle
		// them as a special case: 
		
		if (st.hasMoreTokens()) 
		{
		    String token  = st.nextToken();
		    String name = token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR));
		    String value = token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length());
		    domainStore.put(name, cookie);
		    cookie.put(name, value);
		}
    
		while (st.hasMoreTokens())
		{
			try
			{
			    String token  = st.nextToken();
			    String name = token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR)).toLowerCase(Locale.US);
			    String value = token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length());
			    cookie.put(name,value);
			}
			catch(Exception e)
			{
				SLog.e(TAG,e);
			}
			
		}
    }
    
    /**
     * Retrieves and stores cookies returned by the host on the other side
     * of the the open java.net.URLConnection.
     *
     * The connection MUST have been opened using the connect()
     * method or a IOException will be thrown.
     *
     * @param conn a java.net.URLConnection - must be open, or IOException will be thrown
     * @throws java.io.IOException Thrown if conn is not open.
     */
    @SFSDKDefaultAccessScope void readCookiesFromConnection(URLConnection conn) throws IOException 
    {
	
    	Map<String,Map<String,String>> domainStore = getDomainStoreFromHost(conn.getURL().getHost());
		
		// OK, now we are ready to get the cookies out of the URLConnection	
		String headerName=null;
		for (int i=1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) 
		{
		    if (headerName.equalsIgnoreCase(SET_COOKIE)) 
		    {
				storeCookieToDomainStore(domainStore, conn.getHeaderField(i));
		    }
		}
    }
 

    /**
     * Prior to opening a URLConnection, calling this method will set all
     * unexpired cookies that match the path or subpaths for the underlying URL
     *
     * The connection MUST NOT have been opened 
     * method or an IOException will be thrown.
     *
     * @param conn a java.net.URLConnection - must NOT be open, or IOException will be thrown
     * @throws java.io.IOException Thrown if conn has already been opened.
     */
    @SFSDKDefaultAccessScope void setCookies(URLConnection conn) throws IOException 
    {
	
		// let's determine the domain and path to retrieve the appropriate cookies
		URL url = conn.getURL();
		String domain = getDomainFromHost(url.getHost());
		String path = url.getPath();
		
		Map<?, ?> domainStore = mStore.get(domain);
		if (domainStore == null) return;
		StringBuffer cookieStringBuffer = new StringBuffer();
		
		Iterator<?> cookieNames = domainStore.keySet().iterator();
		while(cookieNames.hasNext()) 
		{
		    String cookieName = (String)cookieNames.next();
		    Map<?, ?> cookie = (Map<?, ?>)domainStore.get(cookieName);
		    // check cookie to ensure path matches  and cookie is not expired
		    // if all is cool, add cookie to header string 
		    if (comparePaths((String)cookie.get(PATH), path) && isNotExpired((String)cookie.get(EXPIRES))) 
		    {
				cookieStringBuffer.append(cookieName);
				cookieStringBuffer.append("=");
				cookieStringBuffer.append((String)cookie.get(cookieName));
				if (cookieNames.hasNext()) cookieStringBuffer.append(SET_COOKIE_SEPARATOR);
		    }
		}
		
		try 
		{
		    conn.setRequestProperty(COOKIE, cookieStringBuffer.toString());
		}
		catch (java.lang.IllegalStateException ise) 
		{
		    IOException ioe = new IOException("Illegal State! Cookies cannot be set on a URLConnection that is already connected. " 
		    + "Only call setCookies(java.net.URLConnection) AFTER calling java.net.URLConnection.connect().");
		    throw ioe;
		}
    }

    private String getDomainFromHost(String host) 
    {
    	if (host.indexOf(DOT) != host.lastIndexOf(DOT)) 
    	{
    		return host.substring(host.indexOf(DOT) + 1);
    	} 
    	else 
    	{
    		return host;
    	}
    }

    private boolean isNotExpired(String cookieExpires) 
    {
		if (cookieExpires == null) return true;
		Date now = new Date();
		try 
		{
			return (now.compareTo(dateFormat.parse(cookieExpires))) <= 0;
		} 
		catch (java.text.ParseException pe) 
		{
			SLog.e("Exception",pe);
			return false;
		}
    }

    private boolean comparePaths(String cookiePath, String targetPath) 
    {
		if (cookiePath == null) 
		{
		    return true;
		}
		else if (cookiePath.equals("/")) 
		{
			return true;
		} 
		else if (targetPath.regionMatches(0, cookiePath, 0, cookiePath.length())) 
		{
			return true;
		}
		else 
		{
			return false;
		}	
    }
    
    /**
     * Returns a string representation of stored cookies organized by domain.
     */

    public String toString() 
    {
    	return mStore.toString();
    }
    
    private void removeCookiesForDomain(String domain)
    {
    	mStore.remove(domain);
    }
        
    /**
     *  removes all cookies for the domain in the given URI
     */
    public void removeCookies(URI uri)
    {
    	removeCookiesForDomain(getDomainFromHost(uri.getHost()));    	
    }
     
    /**
     *  removes all cookies for the domain in the given URI
     */
    public void removeCookies(String urlstr)
    {    	
		try 
		{
			URI uri = new URI(urlstr);
			removeCookies(uri);
		} 
		catch (URISyntaxException e) 
		{			
			SLog.e(TAG,e);
		}    	    	
    }
}