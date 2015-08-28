package com.citrix.sharefile.api.exceptions;

public class LocalizedError
{
    public static String get(Exception e, String defaultValue)
    {
        if(e instanceof SFServerException || e instanceof SFConnectionException)
        {
            return e.getLocalizedMessage();
        }

        return defaultValue;
    }
}