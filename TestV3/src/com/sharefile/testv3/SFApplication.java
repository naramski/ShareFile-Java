package com.sharefile.testv3;

import com.sharefile.api.models.SFVRootType;

import android.app.Application;
import android.webkit.CookieSyncManager;

public class SFApplication extends Application 
{

	@Override
	public void onCreate() 
	{		
		super.onCreate();
		
		CookieSyncManager.createInstance(this);
		
		String s = SFVRootType.Account.toString();		
	}
}
