package com.sharefile.testv3;

import com.sharefile.java.AsyncJobFactory;
import com.sharefile.java.log.LogLevel;
import com.sharefile.java.log.SLog;

import android.app.Application;
import android.webkit.CookieSyncManager;

public class SFApplication extends Application 
{
	private void setupSLog() {
		@SuppressWarnings("rawtypes")
		AsyncJobFactory asfactory = new AsyncJobFactory();
		AsyncJobFactory.setInstance(asfactory);
									
		SLog.getLogAgency().setLogLevel(LogLevel.V);
		SLog.getLogAgency().getCollector().addWriter(new SLogSystemLogWriter(), true);
				
	}

	@Override
	public void onCreate() 
	{		
		super.onCreate();
		
		CookieSyncManager.createInstance(this);
				
		setupSLog();				
	}
}
