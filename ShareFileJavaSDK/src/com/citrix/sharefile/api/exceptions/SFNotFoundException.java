package com.citrix.sharefile.api.exceptions;

import javax.net.ssl.HttpsURLConnection;

public class SFNotFoundException extends SFServerException
{
    public SFNotFoundException(String detailedMessage)
    {
        super(HttpsURLConnection.HTTP_NOT_FOUND,detailedMessage);
    }
}