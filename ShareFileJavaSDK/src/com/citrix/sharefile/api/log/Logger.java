package com.citrix.sharefile.api.log;

import com.citrix.sharefile.api.interfaces.ILog;

public class Logger {
    private static ILog logInstance = new SilentLog();
    public static void set(ILog log) {
        if (log==null) log = new SilentLog();
        logInstance = log;
    }

    public static int v(String tag, String msg) {
        return logInstance.v(tag,msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return logInstance.v(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        return logInstance.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return logInstance.d(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return logInstance.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return logInstance.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return logInstance.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return logInstance.w(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        return logInstance.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        return logInstance.e(tag, msg);
    }

    public static int e(String tag, Throwable tr) {
        return logInstance.e(tag,tr);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return logInstance.e(tag, msg, tr);
    }

}
