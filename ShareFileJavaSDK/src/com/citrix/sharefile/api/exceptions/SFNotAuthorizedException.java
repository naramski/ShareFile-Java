package com.citrix.sharefile.api.exceptions;


import com.citrix.sharefile.api.SFReAuthContext;

public class SFNotAuthorizedException extends SFSDKException
{
    private SFReAuthContext mReAuthContext;

    private SFFormsAuthenticationCookies mFormsAuthenticationCookies;

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
        this.mFormsAuthenticationCookies = null;
    }

    public SFNotAuthorizedException(Exception e, SFReAuthContext reAuthContext)
    {
        super(e);
        mReAuthContext = reAuthContext;
        this.mFormsAuthenticationCookies = null;
    }

    public SFNotAuthorizedException(String detailedMessage, SFFormsAuthenticationCookies mFormsAuthenticationCookies, SFReAuthContext reAuthContext) {
        super(detailedMessage);
        this.mFormsAuthenticationCookies = mFormsAuthenticationCookies;
        this.mReAuthContext = reAuthContext;
    }

    public SFFormsAuthenticationCookies getFormsAuthenticationCookies() {
        return mFormsAuthenticationCookies;
    }

    public SFReAuthContext getReAuthContext()
    {
        return mReAuthContext;
    }
}