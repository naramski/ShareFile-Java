package com.citrix.sharefile.api.authentication;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.citrix.sharefile.api.constants.SFKeywords;
import com.citrix.sharefile.api.constants.SFSdkGlobals;
import com.citrix.sharefile.api.exceptions.SFJsonException;
import com.citrix.sharefile.api.gson.SFGsonHelper;

/**
 *  Immutable SFOAuth2Token
 */
public final class SFOAuth2Token
{
	private final String mAccessToken;
	private final String mRefreshToken;
	private final String mTokenType;
	private final String mAppcp;
	private final String mApicp;
	private final String mSubdomain;
	private long mExpiresIn;
	
	/**
	 *   Make this a more stronger check than a simple null check for feilds
	 */
	public boolean isValid()
	{
		if(mAccessToken == null) return false;
		if(mRefreshToken == null) return false;
		if(mTokenType == null) return false;
		if(mAppcp == null) return false;
		if(mApicp == null) return false;
		if(mSubdomain == null) return false;
		if(mExpiresIn < 0) return false;		
		
		return true;
	}
	
	public String getAccessToken()
	{
		return mAccessToken;
	}
	
	public String getRefreshToken()
	{
		return mRefreshToken;
	}
	
	public String getTokenType()
	{
		return mTokenType;
	}
	
	public String getAppCP()
	{
		return mAppcp;
	}
	
	public String getApiCP()
	{
		return mApicp;
	}
	
	public String getSubdomain()
	{
		return mSubdomain;
	}
	
	public long getExpiryTime()
	{
		return mExpiresIn;
	}
	
	/**
	 *   subdomain.sf-api.com
	 */
	public String getApiServer()
	{
		return mSubdomain +"." + SFSdkGlobals.getApiServer(mApicp);
	}
			
	public SFOAuth2Token(JsonObject json) 
	{
		if(json!=null)
		{					
			mAccessToken = SFGsonHelper.getString(json,SFKeywords.ACCESS_TOKEN , null);
			mRefreshToken = SFGsonHelper.getString(json,SFKeywords.REFRESH_TOKEN , null);
			mTokenType = SFGsonHelper.getString(json,SFKeywords.TOKEN_TYPE , null);
			mAppcp = SFGsonHelper.getString(json,SFKeywords.APP_CP , null);
			mApicp = SFGsonHelper.getString(json,SFKeywords.API_CP , null);
			mSubdomain = SFGsonHelper.getString(json,SFKeywords.SUBDOMAIN , null);
			mExpiresIn = SFGsonHelper.getLong(json,SFKeywords.EXPIRES_IN , 0);
		}
		else
		{
			throw new JsonParseException("NULL Object given to parser");
		}
	}
	
	public SFOAuth2Token(String jsonString) throws SFJsonException
	{				
		this(new JsonParser().parse(jsonString).getAsJsonObject());
	}
		
	public String toJsonString() throws SFJsonException 
	{		
		JsonObject jsonObject = new JsonObject();
		
		jsonObject.addProperty(SFKeywords.ACCESS_TOKEN, mAccessToken );
		jsonObject.addProperty(SFKeywords.REFRESH_TOKEN,mRefreshToken );
		jsonObject.addProperty(SFKeywords.TOKEN_TYPE,mTokenType );
		jsonObject.addProperty(SFKeywords.APP_CP,mAppcp );
		jsonObject.addProperty(SFKeywords.API_CP,mApicp );
		jsonObject.addProperty(SFKeywords.SUBDOMAIN,mSubdomain );
		jsonObject.addProperty(SFKeywords.EXPIRES_IN,mExpiresIn );
		
		return jsonObject.toString();
	}			
	
	public SFOAuth2Token(String accessToken,
			String refreshToken,
			String tokenType,
			String appcp,
			String apicp,
			String subdomain,
			long expiresIn)
	{
		
		mAccessToken = accessToken;
		mRefreshToken = refreshToken;
		mTokenType = tokenType;
		mAppcp = appcp;
		mApicp = apicp;
		mSubdomain = subdomain;
		mExpiresIn = expiresIn;		
	}
				
}