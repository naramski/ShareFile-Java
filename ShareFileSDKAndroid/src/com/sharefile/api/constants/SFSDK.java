package com.sharefile.api.constants;

public class SFSDK 
{
	public static final String VERSION_FOR_QUERY_URL = "v3";
	public static final String VERSION= "V3.1.0";
	
	public static final String API_SERVER_TEST = "sf-apitest.com";
	public static final String API_SERVER_DEV = "sf-apidev.com";
	public static final String API_SERVER_PRODUCTION = "sf-api.com";
	public static final String API_SERVER_PRODUCTION_EU = "sf-api.eu";
		
	public static final String[] mApiServer = {API_SERVER_PRODUCTION, API_SERVER_DEV,API_SERVER_TEST,API_SERVER_PRODUCTION_EU};
		
	private static final int HOST_INDEX_PRODUCTION = 0;
	private static final int HOST_INDEX_DEV 		  = 1;
	private static final int HOST_INDEX_TEST 	  = 2;
	private static final int HOST_INDEX_PRODUCTION_EU 	  = 3;
		
	public static final int getHostIndex(final String hostName)
	{
		int index = HOST_INDEX_PRODUCTION;
		
		if(hostName.endsWith("dev.com"))
		{
			index = HOST_INDEX_DEV;
		}
		else if(hostName.endsWith("test.com"))
		{
			index = HOST_INDEX_TEST;
		}
		else if(hostName.endsWith(".eu"))
		{
			index = HOST_INDEX_PRODUCTION_EU;
		}
		
		return index;
	}
	
	public static final String getApiServer(String hostname)
	{
		return mApiServer[getHostIndex(hostname)];
	}
	
	public static final int INTERNAL_HTTP_ERROR = 599; //last http error code i s 505. Lets start our internal errors by leaving some difference.
	public static final int HTTP_ERROR_CANCELED = 600;
}