package com.citrix.sharefile.api;

public class SFCredential
{
    private final String mUserName;
    private final String mPassword;

    public SFCredential(String username, String password)
    {
        mUserName = username;
        mPassword = password;
    }

    public String getUserName()
    {
        return mUserName;
    }

    public String getPassword()
    {
        return mPassword;
    }
}