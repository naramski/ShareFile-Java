package com.sharefile.api.exceptions;

public class SFNotFoundException extends SFServerException
{
    public SFNotFoundException(String detailedMessage)
    {
        super(detailedMessage);
    }
}