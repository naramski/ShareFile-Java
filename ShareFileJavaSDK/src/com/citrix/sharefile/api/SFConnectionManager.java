package com.citrix.sharefile.api;

import com.citrix.sharefile.api.interfaces.ISFConnectionManager;
import com.citrix.sharefile.api.log.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class SFConnectionManager
{
    private static final String TAG = "SFConnMgr";

    private static final ISFConnectionManager DEFAULT = new ISFConnectionManager() {
        @Override
        public void onBeforeConnect(URLConnection connection) {}

        @Override
        public void onConnectException(URLConnection connection, IOException e) {}

        @Override
        public InputStream getInputStream(URLConnection conn) throws IOException {
            return conn.getInputStream();
        }
    };

    private static ISFConnectionManager mInstance = DEFAULT;

    private static void onBeforeConnect(URLConnection connection) {
        mInstance.onBeforeConnect(connection);
    }

    private static void onConnectException(URLConnection connection, IOException e) {
        mInstance.onConnectException(connection, e);
    }

    public static void connect(URLConnection connection) throws IOException {
        try {
            connection.connect();

        } catch (IOException e) {
            onConnectException(connection, e);
            throw e;
        }
    }

    public static URLConnection openConnection(URL url) throws IOException {
        try {
            Logger.v(TAG, "Open Connection to: " + url.toString());
            URLConnection connection = url.openConnection();
            onBeforeConnect(connection);
            return connection;

        } catch (IOException e) {
            Logger.v(TAG, ">> failed to open connection to: " + url.toString(), e);
            onConnectException(null, e);
            throw e;
        }
    }

    public static InputStream getInputStream(URLConnection conn) throws IOException {
        try {
            return mInstance.getInputStream(conn);

        } catch (IOException e) {
            Logger.v(TAG, ">> failed to get input stream: " + conn.getURL().toString(), e);
            onConnectException(conn, e);
            throw e;

        }
    }

    @SFSDKDefaultAccessScope static void setInstance(ISFConnectionManager newConnectionMgr)
    {
        if(newConnectionMgr == null)
        {
            mInstance = DEFAULT;
            return;
        }

        mInstance = newConnectionMgr;
    }
}
