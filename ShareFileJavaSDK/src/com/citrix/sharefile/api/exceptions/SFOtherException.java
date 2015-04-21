package com.citrix.sharefile.api.exceptions;

public class SFOtherException extends SFSDKException
{
    public SFOtherException(Throwable ex)
    {
        super(ex);
    }

    public SFOtherException(String detailedMessage)
    {
        super(detailedMessage);
    }
}
