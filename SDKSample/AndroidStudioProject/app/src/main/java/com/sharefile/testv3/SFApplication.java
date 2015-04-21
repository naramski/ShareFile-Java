package com.sharefile.testv3;

import android.app.Application;
import android.widget.Toast;

import com.citrix.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.testv3.Core.Core;

public class SFApplication extends Application 
{
    private static final String TAG = "SFApplication";

	@Override
	public void onCreate() 
	{		
		super.onCreate();

        try
        {
            Core.initShareFileSDK();
        }
        catch (SFInvalidStateException e)
        {
            Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
        }
    }
}