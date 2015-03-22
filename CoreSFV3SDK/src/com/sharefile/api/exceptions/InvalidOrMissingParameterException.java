package com.sharefile.api.exceptions;

public class InvalidOrMissingParameterException extends RuntimeException
{
    public InvalidOrMissingParameterException(String msg)
    {
        super(new RuntimeException(msg));
    }

    public InvalidOrMissingParameterException(Exception ex)
    {
        super(ex);
    }
}