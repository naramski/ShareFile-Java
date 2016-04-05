package com.citrix.sharefile.api.exceptions;

/**
 * Created by sai on 2/22/16.
 */
public class SFFormsAuthenticationCookies {

    private String loginURL;
    private String token;

    public void setLoginURL(String loginURL) {
        this.loginURL = loginURL;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLoginURL() {
        return loginURL;
    }

    public String getToken() {
        return token;
    }
}
