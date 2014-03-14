package com.sharefile.api.authentication;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;
import com.sharefile.api.exceptions.SFJsonException;
import com.sharefile.api.gson.SFGsonHelper;
import com.sharefile.api.interfaces.SFJsonInterface;
import com.sharefile.api.models.SFODataObject;

public class SFOAuth2Token implements SFJsonInterface
{
	private String mAccessToken = null;
	private String mRefreshToken = null;
	private String mTokenType = null;
	private String mAppcp = null;
	private String mApicp = null;
	private String mSubdomain = null;
	private long mExpiresIn = 0;
	
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
		if(mExpiresIn == 0) return false;		
		
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
		return mSubdomain +"." + SFSDK.API_SERVER;
	}
			
	public SFOAuth2Token(JsonObject json) 
	{
		parse(json);
	}
	
	public SFOAuth2Token(String jsonString) throws SFJsonException
	{
		parseFromJson(jsonString);
	}

	@Override
	public void parseFromJson(String jsonString) throws SFJsonException 
	{				        
        try
        {           	        	
        	SFGsonHelper.fromJson(jsonString, SFOAuth2Token.class, new TypeToken<SFOAuth2Token>(){}.getType(), new SFGsonDeserializer());
        }
        catch(JsonSyntaxException ex)
        {
        	throw new SFJsonException(ex);
        }        		
	}
	
	@Override
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
	
	private class SFGsonDeserializer implements JsonDeserializer<Object>
	{
		@Override
		public SFODataObject deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext context) throws JsonParseException 
		{
			if (jsonelement != null) 
			{
				JsonObject json = jsonelement.getAsJsonObject();				
				parse(json);
			}
			
			return null;						
		}		
	}
	
	private void parse(JsonObject json) throws JsonParseException
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
}