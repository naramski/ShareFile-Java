package com.sharefile.api.constants;

public final class SFKeywords 
{
	public static final String TAG = "SFSDK";
	
	//SFOdataObject related keywords
	public static final String ODATA_METADATA = "odata.metadata";
	public static final String ODATA_COUNT = "odata.count";
	public static final String ODATA_NEXTLINK = "odata.nextLink";
	
	public static final String URL = "url";
	public static final String Id = "Id";
	public static final String DownloadUrl = "DownloadUrl";	
	public static final String Range = "Range";	
	
	public static final String PRINCIPAL = "Principal";
	public static final String AUTHENTICATION_TYPE = "AuthenticationType";
	public static final String TOOL = "Tool";
	public static final String VERSION = "Version";
	public static final String IS_AUTHENTICATED = "IsAuthenticated";
	public static final String NAME = "NAME";
	public static final String Name = "Name";
	public static final String DEVICE_USER = "DEVICE_USER";
	public static final String Description = "Description";
	public static final String Zone = "Zone";
	
	public static final String CODE = "code";
	public static final String VALUE = "value";
	public static final String MESSAGE = "message";
	
	public static final String CHILDREN = "Children";
	public static final String PARENT = "Parent";
	public static final String REDIRECTION = "Redirection";
			
	//Access token related
	public static final String ACCESS_TOKEN = "access_token";
	public static final String REFRESH_TOKEN = "refresh_token";
	public static final String TOKEN_TYPE = "token_type";
	public static final String APP_CP = "appcp";
	public static final String API_CP = "apicp";
	public static final String SUBDOMAIN = "subdomain";
	public static final String EXPIRES_IN = "expires_in";
	
	public static final String GRANT_TYPE = "grant_type";
	public static final String CLIENT_ID = "client_id";
	public static final String CLIENT_SECRET = "client_secret";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	
	//Http headers and properties related	
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
	public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";	
	public static final String APPLICATION_JSON = "application/json";
	public static final String UTF_8 = "UTF-8";
	
	public static final String PREFIX_HTTPS = "https://";
	public static final String PREFIX_HTTP = "http://";
	public static final String FWD_SLASH = "/";
	public static final String OPEN_BRACKET = "(";
	public static final String CLOSE_BRACKET = ")";
	public static final String EQUALS = "=";
	public static final String COMMA = ",";
	public static final String COLON = ":";
	public static final String EMPTY = "";
	public static final String CHAR_QUERY = "?";
	public static final String CHAR_DOLLAR = "$";
	public static final String CHAR_AMPERSAND = "&";
	
	
	public static final String Download = "Download";
	public static final String Items = "Items";
	
	//Informative stack trace mesage for exceptions thrown by us
	public static final String EXCEPTION_MSG_INVALID_PARAMETER_TO_QUERY = "setId and addIds are multually exclusive. only one of them can be called on a query object";
	public static final String EXCEPTION_MSG_NOT_IMPLEMENTED = "function not implemented";

	public static final String Location = "Location";
		
	public static final String ACCEPT_LANGUAGE = "Accept-Language";

	public static final String FileName = "FileName";

	public static final String CreationDate = "CreationDate";
	
	public static final String PREFIX_PERSONAL_CONNECTOR = "c-";
}