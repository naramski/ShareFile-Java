package com.sharefile.testv3;


import android.util.Log;

import com.sharefile.api.interfaces.ILog;

public class SFLogger implements ILog
{
    @Override
    public int v(String tag, String msg)
    {
        return Log.v("SF_"+tag, msg);
    }

    @Override
    public int v(String tag, String msg, Throwable tr) {
        return Log.v("SF_"+tag, msg);
    }

    @Override
    public int d(String tag, String msg) {
        return Log.d("SF_"+tag, msg);
    }

    @Override
    public int d(String tag, String msg, Throwable tr) {
        return Log.d("SF_"+tag, msg,tr);
    }

    @Override
    public int i(String tag, String msg) {
        return Log.i("SF_"+tag, msg);
    }

    @Override
    public int i(String tag, String msg, Throwable tr) {
        return Log.i("SF_"+tag, msg,tr);
    }

    @Override
    public int w(String tag, String msg) {
        return Log.w("SF_"+tag, msg);
    }

    @Override
    public int w(String tag, String msg, Throwable tr) {
        return Log.w("SF_"+tag, msg);
    }

    @Override
    public int w(String tag, Throwable tr) {
        return Log.w("SF_"+tag, tr);
    }

    @Override
    public int e(String tag, String msg) {
        return Log.e("SF_"+tag, msg);
    }

    @Override
    public int e(String tag, Throwable tr) {
        return Log.v("SF_"+tag, tr.getLocalizedMessage());
    }

    @Override
    public int e(String tag, String msg, Throwable tr) {
        return Log.v("SF_"+tag, msg,tr);
    }
}