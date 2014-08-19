package com.sharefile.api.https;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sharefile.api.SFConfiguration;
import com.sharefile.api.SFV3Error;
import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.enumerations.SFRedirectionType;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFV3ErrorException;
import com.sharefile.api.gson.SFGsonHelper;
import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFFolder;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.models.SFRedirection;
import com.sharefile.api.models.SFSymbolicLink;
import com.sharefile.api.utils.SFDumpLog;
import com.sharefile.java.log.SLog;

/**
 *   This class provides two methods: executeQuery() and executeBlockingQuery() to execute the V3 api calls.
 *   <br>executeQuery() is launched on a separate thread and internally calls  executeBlockingQuery() to get its work done.
 */
public class SFApiRunnable<T extends SFODataObject> implements Runnable 
{
	private static final String TAG = SFKeywords.TAG + "-SFApiThread";
			
	private ISFQuery<T> mQuery; 
	private final SFApiResponseListener<T> mResponseListener;
	private final SFOAuth2Token mOauthToken;
	private final SFCookieManager mCookieManager;
	private final SFConfiguration mAppSpecificConfig;
	
	
	private final class Response
	{
		SFODataObject returnObject;
		SFV3Error errorObject;
		
		public void setResponse(SFODataObject ret,SFV3Error err)
		{
			returnObject = ret;
			errorObject = err;
		}
	}
	
	private final Response mResponse = new Response();
						
	public SFApiRunnable(ISFQuery<T> query, SFApiResponseListener<T> responseListener, SFOAuth2Token token, SFCookieManager cookieManager, SFConfiguration config) throws SFInvalidStateException
	{			
		mQuery = query;
		mResponseListener = responseListener;
		mOauthToken = token;		
		mCookieManager = cookieManager;
		mAppSpecificConfig = config;
	}
	
	@Override
	public void run() 
	{		
		executeQuery();		
	}
	
	private void handleHttPost(URLConnection conn) throws IOException
	{
		if(mQuery.getHttpMethod().equalsIgnoreCase(SFHttpMethod.POST.toString()))
		{
			String body = mQuery.getBody(); 
			
			if(body!=null)
			{
				conn.setRequestProperty(SFKeywords.CONTENT_LENGTH, ""+body.getBytes().length);
				conn.setRequestProperty(SFKeywords.CONTENT_TYPE, SFKeywords.APPLICATION_JSON);
				
				SFHttpsCaller.postBody(conn, body);				
			}
		}
	}
				
	private void executeQuery() 
	{
		try
		{
			 executeBlockingQuery();
			 callResponseListeners(mResponse.returnObject, mResponse.errorObject);
		}
		catch(Exception e)
		{
			SLog.e(TAG, "Exception. This should not happen." , e);
		}
	}
		
	public SFODataObject executeBlockingQuery() throws SFV3ErrorException 
	{			
		int httpErrorCode =  SFSDK.INTERNAL_HTTP_ERROR;
		String responseString = null;
		URLConnection connection = null;		
		
		try
		{
			String server = mOauthToken.getApiServer();		
			String urlstr = mQuery.buildQueryUrlString(server);
							
			URL url = new URL(urlstr);
			connection = SFHttpsCaller.getURLConnection(url);		
			SFHttpsCaller.setMethod(connection, mQuery.getHttpMethod());
			mAppSpecificConfig.setAddtionalHeaders(connection);
			
			SFHttpsCaller.addAuthenticationHeader(connection,mOauthToken,mQuery.getUserName(),mQuery.getPassword(),mCookieManager);
			
			handleHttPost(connection);
			
			SLog.d(TAG, mQuery.getHttpMethod() + " " + urlstr);
			
			connection.connect();
			
			httpErrorCode = SFHttpsCaller.safeGetResponseCode(connection);			
						
			SFHttpsCaller.getAndStoreCookies(connection, url,mCookieManager);
			
			switch(httpErrorCode)
			{
				case HttpsURLConnection.HTTP_OK:
				{															
					responseString = SFHttpsCaller.readResponse(connection);
					SFDumpLog.dumpLog(TAG, "RAW RESPONSE = " , responseString);
					
					SFODataObject ret = callSuccessResponseParser(responseString);
					
					if(ret!=null)
					{
						mResponse.setResponse(ret, null);
					}
					else
					{
						if(mResponse.errorObject == null)
						{
							mResponse.setResponse(null, new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR,null,null));
						}
					}
				}
				break;
				
				case HttpsURLConnection.HTTP_NO_CONTENT:
				{
					mResponse.setResponse(null, null);
				}
				break;
				
				case HttpsURLConnection.HTTP_UNAUTHORIZED:
				{
					SFDumpLog.dumpLog(TAG, "RAW RESPONSE = ", "AUTH ERROR");
					SFV3Error sfV3error = new SFV3Error(httpErrorCode,null,null);
					mResponse.setResponse(null, sfV3error);
				}
				break;
				
				default:
				{
					responseString = SFHttpsCaller.readErrorResponse(connection);
					SFDumpLog.dumpLog(TAG, "RAW RESPONSE = " , responseString);
					SFV3Error sfV3error = new SFV3Error(httpErrorCode,responseString,null);
					mResponse.setResponse(null, sfV3error);
				}
			}		    							    															
		}
		catch(Exception ex)
		{		
			SLog.e(TAG,ex);
			SFV3Error sfV3error = new SFV3Error(SFSDK.INTERNAL_HTTP_ERROR, null, ex);
			mResponse.setResponse(null, sfV3error);
		}		
		finally
		{
			SFHttpsCaller.disconnect(connection);
		}
								
		return returnResultOrThrow(mResponse.returnObject,mResponse.errorObject);
	}		
	
	private SFODataObject executeQueryOnRedirectedObject(SFRedirection redirection) 
	{
		SFODataObject odataObject = null;
		
		try 
		{						
			URI redirectLink = redirection.getUri();
			
			SLog.d(TAG,"REDIRECT TO: " + redirectLink);
			
			mQuery.setFullyParametrizedLink(redirectLink);
			
			odataObject = executeBlockingQuery();
		} 
		catch (Exception e) 
		{			
			SLog.e(TAG,e);
		}
		
		return odataObject;
	}
		
	/**
	 *   If the returned object is a Symbolic Link or has a Redirection feild we need to read ahead to get the actual contents.
	 * @throws URISyntaxException 
	 * @throws SFV3ErrorException 
	 *   
	 */
	
	private boolean mAlreadRedirecting = false;
	
	private SFRedirectionType redirectionRequired(SFODataObject object)
	{
		SFRedirectionType ret = SFRedirectionType.NONE;
		
		if(!mQuery.readAheadAllowed())
		{
			return ret;
		}
		
		if((object == null) )
		{
			return ret;
		}
				
		if(object instanceof SFSymbolicLink)
		{
			ret = SFRedirectionType.SYMBOLIC_LINK;
		}
		else if(object instanceof SFFolder)
		{
			SFFolder folder = (SFFolder) object;
			
			Boolean hadRemoteChildren = folder.getHasRemoteChildren();
			
			if(folder.getRedirection()!=null && hadRemoteChildren!=null && hadRemoteChildren == true && !mAlreadRedirecting)
			{					
				ret = SFRedirectionType.FOLDER_ENUM;
				mAlreadRedirecting = true;
			}
		}
		else if(object instanceof SFRedirection)
		{
			ret = SFRedirectionType.URI;				
		}		
		
		return ret;
	}
	
	private SFODataObject executeQueryOnSymbolicLink(SFSymbolicLink link) throws URISyntaxException, SFV3ErrorException
	{
		mQuery.setLink(link.getLink().toString());		
		return executeBlockingQuery();
	}
	
	@SuppressWarnings("unchecked")
	protected void callResponseListeners(SFODataObject sfobject,SFV3Error v3error)
	{
		if(mResponseListener == null)
		{
			return;
		}
				
		try
		{
			if(v3error !=null)
			{
				mResponseListener.sfApiError(v3error, mQuery);				
			}
			else
			{
				mResponseListener.sfApiSuccess((T) sfobject);
			}						
		}
		catch(Exception ex)
		{
			SLog.e(TAG,ex);
		}
	}
	
	private SFODataObject returnResultOrThrow(SFODataObject sfobject,SFV3Error v3error) throws SFV3ErrorException
	{
		//Run this only when the responseListener is not installed.
		if(mResponseListener != null)
		{
			return sfobject;
		}
				
		if(v3error == null)
		{
			return sfobject;
		}
		
		throw new SFV3ErrorException(v3error);
	}
				
				
	/**
	 *  If an error happens during parsing the success response, 
	 *  we return the exception description + the original server response in V3Error Object
	 * @throws URISyntaxException 
	 * @throws SFV3ErrorException 
	 */
	protected SFODataObject callSuccessResponseParser(String responseString) throws SFV3ErrorException, URISyntaxException
	{
		preprocessSuccessResponse(responseString);
							
		JsonParser jsonParser = new JsonParser();
		JsonElement jsonElement =jsonParser.parse(responseString);
		SFODataObject sfobject = SFGsonHelper.customParse(jsonElement);
				
		switch (redirectionRequired(sfobject)) 
		{
			case SYMBOLIC_LINK:
				sfobject = executeQueryOnSymbolicLink((SFSymbolicLink)sfobject);
			break;
					
			case FOLDER_ENUM:	
				sfobject = executeQueryOnRedirectedObject(((SFFolder)sfobject).getRedirection());
			break;
				
			case URI:
				sfobject = executeQueryOnRedirectedObject((SFRedirection) sfobject);							
			break;
				
			case NONE:
				//do nothing
			break;
		}
				
		return sfobject;
	}
	
	protected void preprocessSuccessResponse(String responseString)
	{
		
	}
	
	public Thread startNewThread()
	{
		Thread sfApithread = new Thread(this);		
		sfApithread.start();
		return sfApithread;
	}
	
	protected SFApiResponseListener<T> getResponseListener()
	{
		return mResponseListener;
	}		
}