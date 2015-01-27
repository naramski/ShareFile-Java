package com.sharefile.api.authentication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.enumerations.SFHttpMethod;
import com.sharefile.api.exceptions.SFJsonException;
import com.sharefile.java.log.SLog;

public class SFOAuthSimpleAuthenticator 
{
	
	private static final String TAG = SFKeywords.TAG + "-simpleauth";

	/**
	 * Authenticate via username/password
	 * 
	 * @param hostname
	 *            - hostname like "myaccount.sharefile.com"
	 * @param clientId
	 *            - your client id
	 * @param clientSecret
	 *            - your client secret
	 * @param username
	 *            - my@user.name
	 * @param password
	 *            - mypassword
	 * @return an OAuth2Token instance
	 * @throws SFJsonException 
	 */
	public static SFOAuth2Token authenticate(String hostname, String clientId,String clientSecret, String username, String password)
			throws IOException, SFJsonException 
	{
		URL grantUrl = new URL(String.format("https://%s/oauth/token", hostname));

		HashMap<String, String> params = new HashMap<String, String>();
		StringBuilder queryString = new StringBuilder();

		params.put(SFKeywords.GRANT_TYPE, SFKeywords.PASSWORD);
		params.put(SFKeywords.CLIENT_ID, clientId);
		params.put(SFKeywords.CLIENT_SECRET, clientSecret);
		params.put(SFKeywords.USERNAME, username);
		params.put(SFKeywords.PASSWORD, password);
		
		for (Map.Entry<String, String> entry : params.entrySet()) 
		{
			queryString.append(String.format("%s=%s&", entry.getKey(),URLEncoder.encode(entry.getValue(),SFKeywords.UTF_8)));
		}
		
		queryString.deleteCharAt(queryString.length() - 1);
		//SLog.d(TAG,"%s", queryString);

		HttpsURLConnection connection = (HttpsURLConnection) grantUrl
				.openConnection();
		connection.setRequestMethod(SFHttpMethod.POST.toString());
		connection.addRequestProperty(SFKeywords.CONTENT_TYPE,SFKeywords.APPLICATION_FORM_URLENCODED);
		
		connection.setDoOutput(true);		
		connection.connect();

		connection.getOutputStream().write(queryString.toString().getBytes());

		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder response = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			response.append(line);
		}
		
		if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK)
		{
			return new SFOAuth2Token(response.toString());
		}
		/*
		JsonObject token = null;
		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			JsonParser parser = new JsonParser();
			token = (JsonObject) parser.parse(response.toString());
		}
		// OAuth2Token oauth2Token = new OAuth2Token(token);
		 */
		
		return null;
	}
}
