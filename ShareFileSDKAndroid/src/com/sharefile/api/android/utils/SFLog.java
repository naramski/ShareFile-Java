package com.sharefile.api.android.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.constants.SFSDK;

import android.os.Environment;
import android.util.Log;

/**
 *   Intelligent thin wrapper around the Android Lod.*()  functions to enhance the logging capability.   
 */
public class SFLog 
{
    private static class LogContext 
    {
        LogContext(StackTraceElement element) 
        {            
            m_simpleClassName = getSimpleClassName(element.getClassName());
            m_methodName = element.getMethodName();
            m_lineNumber = element.getLineNumber();
        }
         
        private String m_simpleClassName;
        private String m_methodName;
        private int m_lineNumber;
    }
 
    public enum Level 
    {
        V(1), D(2), I(3), W(4), E(5), DisableLogs(6);
 
        private int value;
 
        private Level(int value) 
        {
            this.value = value;
        }
 
        int getValue() 
        {
            return value;
        }
    };
 
    private static final DateFormat FLOG_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);    
    private static final File LOG_DIR = new File(Environment.getExternalStorageDirectory() + File.separator + "alog");    
    private static boolean fileLogging = false;
    private static String tag = SFKeywords.TAG+SFSDK.VERSION;
    private static Level level = Level.V;
    private static final BlockingQueue<String> logQueue = new LinkedBlockingQueue<String>();
    
    private static Runnable queueRunner = new Runnable() 
    {
        @Override
        public void run() 
        {
            String line;
            try 
            {
                while ((line = logQueue.take()) != null) {
 
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        continue;
                    }
                    if (!LOG_DIR.exists() && !LOG_DIR.mkdirs()) {
                        continue;
                    }
 
                    File logFile = new File(LOG_DIR, tag + ".log");
                    Writer w = null;
                    try {
                        w = new FileWriter(logFile, true);
                        w.write(line);
                        w.close();
                    } catch (IOException e) {
                    } finally {
                        if (w != null) {
                            try {
                                w.close();
                            } catch (IOException e1) {
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
           }
        }
    };
 
    static 
    {
        new Thread(queueRunner).start();
    }
 
    /*private static LogContext getContext() 
    {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        //StackTraceElement element = trace[5]; // frame below us; the caller
        StackTraceElement element = trace[7]; // frame below us; the caller
        LogContext context = new LogContext(element);
        return context;
    }
    */
    
    
    private static LogContext getContext(StackTraceElement[] trace,int level) 
    {       
    	LogContext context = null;
    	
    	try
    	{
    		StackTraceElement element = trace[level]; 
    		context = new LogContext(element);
    	}
    	catch(Exception e)
    	{
    		
    	}
    	
        return context;
    }
 
    private static final String getMessage(String s, Object... args) 
    {
    	try
    	{
	        s = String.format(s, args);
	        
	        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
	                	
	        LogContext c = getContext(trace,4);
	        LogContext c2 = getContext(trace,5);
	        
	        String prefix = "";
	        
	        if(c2!=null)
	        {
	        	prefix = "[" + c2.m_simpleClassName+"."+c2.m_methodName +"]";
	        }
	        
	        String msg = null;
	        
	        if(c!=null)
	        {
	        	msg = prefix +  c.m_simpleClassName + "." + c.m_methodName + "(@"+ c.m_lineNumber + "): " + s;
	        }
	        
	        return msg;
    	}
    	catch(Exception ex)
    	{
    		return ex.getLocalizedMessage();
    	}
                
    }
 
    private static String getSimpleClassName(String className) 
    {
        int i = className.lastIndexOf(".");
        
        if (i == -1) 
        {
            return className;
        }
        
        return className.substring(i + 1);
    }
 
    public static void setLevel(Level l) 
    {
        level = l;
    }
 
    public static void setTag(String t) 
    {
        tag = "ShareFile_"+t;
    }
 
    public static void setFileLogging(boolean enable) 
    {
        fileLogging = enable;
    }
 
    public static void v(String format, Object... args) 
    {
        if (level.getValue() > Level.V.getValue()) 
        {
            return;
        }
        
        String msg = getMessage(format, args);
        
        Log.v(tag, msg);
        
        if (fileLogging) 
        {
            flog(Level.V, msg);
        }
    }
 
    public static void d(String format, Object... args) 
    {
        if (level.getValue() > Level.D.getValue()) 
        {
            return;
        }
        
        String msg = getMessage(format, args);
        
        Log.d(tag, msg);
        
        if (fileLogging) 
        {
            flog(Level.D, msg);
        }
    }
    
    public static void d2(String appendTag, String format, Object... args) 
    {
        if (level.getValue() > Level.D.getValue()) 
        {
            return;
        }
        
        String msg = getMessage(format, args);
        
        //Show in chunks! Log cat will truncate ultra long strings else.
        if (msg.length() > 4000) 
        {       
        	/*
        	Log.d(tag+appendTag, "Start---");
        	
            int chunkCount = msg.length() / 4000;     // integer division
            String TAG = tag+appendTag;
            for (int i = 0; i <= chunkCount; i++) 
            {
                int max = 4000 * (i + 1);
                
                if (max >= msg.length()) 
                {                	                	
                    Log.d(TAG, msg.substring(4000 * i));
                } 
                else 
                {
                    Log.d(TAG, msg.substring(4000 * i, max));
                }
            }
            
            Log.d(tag+appendTag, "---End");*/
        	
        	Log.d(tag+appendTag, msg.substring(0,1000)+ "...[truncated]");
        	
        }
        else
        {
                Log.d(tag+appendTag, msg);
        }
    }
 
    public static void i(String format, Object... args) 
    {
        if (level.getValue() > Level.I.getValue()) 
        {
            return;
        }
        
        String msg = getMessage(format, args);
        
        Log.i(tag, msg);
        
        if (fileLogging) 
        {
            flog(Level.I, msg);
        }
    }
 
    public static void w(String format, Object... args) 
    {
        if (level.getValue() > Level.W.getValue()) 
        {
            return;
        }
        
        String msg = getMessage(format, args);
        
        Log.w(tag, msg);
        
        if (fileLogging) 
        {
            flog(Level.W, msg);
        }
    }
 
    public static void w(String format, Throwable t, Object... args) 
    {
        if (level.getValue() > Level.W.getValue()) 
        {
            return;
        }
        
        String msg = getMessage(format, args);
        
        Log.w(tag, msg, t);
        
        if (fileLogging) 
        {
            flog(Level.W, msg, t);
        }
    }
 
    public static void e(String format, Object... args) 
    {
        if (level.getValue() > Level.E.getValue()) 
        {
            return;
        }
        
        String msg = getMessage(format, args);
        
        Log.e(tag, msg);
        
        if (fileLogging) 
        {
            flog(Level.E, msg);
        }
    }
 
    public static void e(String format, Throwable t, Object... args) 
    {
        if (level.getValue() > Level.E.getValue()) 
        {
            return;
        }
        
        String msg = getMessage(format, args);
        
        Log.e(tag, msg, t);
        
        if (fileLogging) 
        {
            flog(Level.E, msg, t);
        }
    }
 
    public static void trace() 
    {
        try 
        {
            throw new Throwable("dumping stack trace ...");            
        } 
        catch (Throwable t) 
        {
            SFLog.e("trace:", t);
        }
    }
 
    public static String getStackTraceString(Throwable tr) 
    {
        if (tr == null) 
        {
            return "";
        }
 
        Throwable t = tr;
        
        while (t != null) 
        {
            if (t instanceof UnknownHostException) 
            {
                return "";
            }
            
            t = t.getCause();
        }
 
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        return sw.toString();
    }
 
    private static void flog(Level l, String msg) 
    {
        flog(l, msg, null);
    }
 
    private static void flog(Level l, String msg, Throwable t) 
    {
        String timeString = FLOG_FORMAT.format(new Date());
        
        String line = timeString + " " + l.toString() + "/" + tag + ": " + msg + "\n";
        
        if (t != null) 
        {
            line += getStackTraceString(t) + "\n";
        }
        
        logQueue.offer(line);
    }            
}