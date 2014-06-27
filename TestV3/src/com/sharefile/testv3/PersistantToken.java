package com.sharefile.testv3;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.exceptions.SFJsonException;

import android.content.Context;


/**
 *    Uses a app private file to persist the token
 */
public class PersistantToken 
{
	private static final String FILENAME = "sf_auth_token";

	public static void saveToken(Context context, SFOAuth2Token token) throws SFJsonException, IOException
	{
		saveToken(context, token.toJsonString());
	}
	
	public static void saveToken(Context context, String token) throws SFJsonException, IOException
	{
		FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
		fos.write(token.getBytes());
		fos.close();
	}
	
	public static SFOAuth2Token readToken(Context context) throws IOException, SFJsonException
	{
		SFOAuth2Token token = null;
		FileInputStream fis = context.openFileInput(FILENAME);
		byte[] buffer = new byte[1024*8];
		int count = fis.read(buffer);
		if(count>0)
		{
			String jsonString = new String(buffer, 0, count);			
			token = new SFOAuth2Token(jsonString);
		}
		return token;
	}
	
	public static boolean deleteToken(Context context)
	{
		return context.deleteFile(FILENAME);
	}
}
