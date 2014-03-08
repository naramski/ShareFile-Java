package com.sharefile.api.authentication;

import com.google.gson.JsonObject;

public class SFOAuth2Token 
{
	public String accessToken = "";
	public String refreshToken = "";
	public String tokenType = "";
	public String appcp = "";
	public String apicp = "";
	public String subdomain = "";
	public int expiresIn = 0;

	public SFOAuth2Token(JsonObject json) {
		if (json != null) {
			accessToken = json.get("access_token").getAsString();
			refreshToken = json.get("refresh_token").getAsString();
			tokenType = json.get("token_type").getAsString();
			appcp = json.get("appcp").getAsString();
			apicp = json.get("apicp").getAsString();
			subdomain = json.get("subdomain").getAsString();
			expiresIn = json.get("expires_in").getAsInt();
		}
	}
}
