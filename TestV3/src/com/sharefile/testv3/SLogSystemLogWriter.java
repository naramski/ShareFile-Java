package com.sharefile.testv3;

import android.util.Log;

import com.sharefile.java.log.ISLogWriter;
import com.sharefile.java.log.LogLevel;
import com.sharefile.java.log.SLog;
import com.sharefile.java.log.SLogMessage;

public class SLogSystemLogWriter implements ISLogWriter {
	private static final String TAG = "SLogSystemLogWriter";

	@Override
	public boolean write(SLogMessage msg) {
						
		if(msg.level == LogLevel.T)
			Log.d(msg.tag, msg.msg);
		else if(msg.level == LogLevel.V)
			Log.v(msg.tag, msg.msg);
		else if(msg.level == LogLevel.D)
			Log.d(msg.tag, msg.msg);
		else if(msg.level == LogLevel.I)
			Log.i(msg.tag, msg.msg);
		else if(msg.level == LogLevel.W)
			Log.w(msg.tag, msg.msg);
		else if(msg.level == LogLevel.E)
			Log.e(msg.tag, msg.msg);
		else if(msg.level == LogLevel.WTF)
			Log.wtf(msg.tag, msg.msg);
		else
			return false;
		
		return true;
	}

	@Override
	public boolean flush() {
		return true;
	}

	@Override
	public boolean clear() {
		return true;
	}

	@Override
	public boolean write(String str) {
		
		
		try {
		    Runtime.getRuntime().exec(new String[]{"logcat", "-c"});
		    return true;
		} catch (Exception e1) {
		    SLog.e(TAG, e1);
		} 
		return false;
	}
	
	public void reset() {
		
	}
}
