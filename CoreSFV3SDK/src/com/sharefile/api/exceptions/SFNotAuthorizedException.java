package com.sharefile.api.exceptions;


public class SFNotAuthorizedException extends SFSDKException
{
    public SFNotAuthorizedException(String detailedMessage)
    {
        super(detailedMessage);
    }
}