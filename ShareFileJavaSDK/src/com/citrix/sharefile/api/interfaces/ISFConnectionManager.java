package com.citrix.sharefile.api.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public interface ISFConnectionManager
{
    void onBeforeConnect(URLConnection connection);
    void onConnectException(URLConnection connection, IOException e);
    InputStream getInputStream(URLConnection conn) throws IOException;
}