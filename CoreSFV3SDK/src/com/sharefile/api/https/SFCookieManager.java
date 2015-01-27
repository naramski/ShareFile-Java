package com.sharefile.api.https;

import com.sharefile.api.SFSDKDefaultAccessScope;
import com.sharefile.java.log.SLog;

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

public class SFCookieManager 
{
	private static final String TAG = "SFCookieManager";

    public static class Cookie {
        public String value;
        public String path=null;
        public String expires=null;
        public Cookie(String value) {
            this.value = value;
        }
    }
	
	/**
	 *   mStore is the map of all domains and their corresponding cookie stores.
	 *   so if we have cookies from hosts like:	 citrix.sharefile.com   or  domian.host.com
	 */
    private Map<String,Map<String,Cookie>> mStore;

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
		mStore = new HashMap<String,Map<String, Cookie>>();
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
    	Map<String,Cookie> domainStore = getDomainStoreFromHost(uri.getHost());
    	
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
            
    private Map<String,Cookie> getDomainStoreFromHost(String host)
    {
    	// let's determine the domain from where these cookies are being sent
		String domain = getDomainFromHost(host);
				
		Map<String,Cookie> domainStore; // this is where we will store cookies for this domain
		
		// now let's check the store to see if we have an entry for this domain
		if (mStore.containsKey(domain)) 
		{
		    // we do, so lets retrieve it from the store
		    domainStore = mStore.get(domain);
		} 
		else 
		{
		    // we don't, so let's create it and put it in the store
		    domainStore = new HashMap<String, Cookie>();
		    mStore.put(domain, domainStore);    
		}
		
		return domainStore;
    }
    
    private void storeCookieToDomainStore(Map<String,Cookie> domainStore,String cookieString)
    {
        // the specification dictates that the first name/value pair
        // in the string is the cookie name and value, so let's handle
        // them as a special case:

    	Cookie cookie = null;
		StringTokenizer st = new StringTokenizer(cookieString, COOKIE_VALUE_DELIMITER);

        while ( st.hasMoreTokens() ) {
            String token = st.nextToken();
            int index = token.indexOf(NAME_VALUE_SEPARATOR);
            if ( index<0 || index>= token.length() ) {
                if ( cookie==null ) {
                    // e
                    SLog.e(TAG, "Invalid cookie string: " + cookieString);
                    return;
                }
                SLog.v(TAG, "Not a value/pair, ignore for now as we don't use it: " + token);
                continue;
            }

            // value pair
            String name = token.substring(0, index).toLowerCase(Locale.US);
            String value = token.substring(index + 1);

            if ( cookie==null ) {
                cookie = new Cookie(value);
                domainStore.put(name, cookie);
            }

            if ( EXPIRES.equals(name) ) {
                cookie.expires = value;
            } else if ( PATH.equals(name) ) {
                cookie.path = value;
            }
            /*else
            {
                // don't have any use for this part of the cookie
                // ...
            }*/
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
	
    	Map<String,Cookie> domainStore = getDomainStoreFromHost(conn.getURL().getHost());
		
		// OK, now we are ready to get the cookies out of the URLConnection	
		String headerName;
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
		
		Map<String, Cookie> domainStore = mStore.get(domain);
		if (domainStore == null) return;
		StringBuffer cookieStringBuffer = new StringBuffer();
		
		Iterator<?> cookieNames = domainStore.keySet().iterator();
		while(cookieNames.hasNext()) 
		{
		    String cookieName = (String)cookieNames.next();
		    Cookie cookie = domainStore.get(cookieName);
		    // check cookie to ensure path matches  and cookie is not expired
		    // if all is cool, add cookie to header string 
		    if (comparePaths(cookie.path, path) && isNotExpired(cookie.expires) )
		    {
				cookieStringBuffer.append(cookieName);
				cookieStringBuffer.append("=");
				cookieStringBuffer.append(cookie.value);
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
			SLog.e(TAG,pe);
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