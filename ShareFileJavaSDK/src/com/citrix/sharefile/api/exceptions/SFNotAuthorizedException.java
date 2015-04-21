package com.citrix.sharefile.api.exceptions;


import com.citrix.sharefile.api.SFReAuthContext;

public class SFNotAuthorizedException extends SFSDKException
{
    private SFReAuthContext mReAuthContext;

    public SFNotAuthorizedException(String detailedMessage)
    {
        super(detailedMessage);
    }

    public SFNotAuthorizedException(Exception e)
    {
        super(e);
    }

    public SFNotAuthorizedException(String detailedMessage, SFReAuthContext reAuthContext)
    {
        super(detailedMessage);
        mReAuthContext = reAuthContext;
    }

    public SFNotAuthorizedException(Exception e, SFReAuthContext reAuthContext)
    {
        super(e);
        mReAuthContext = reAuthContext;
    }

    public SFReAuthContext getReAuthContext()
    {
        return mReAuthContext;
    }
}