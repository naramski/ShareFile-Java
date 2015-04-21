package com.citrix.sharefile.api.interfaces;

import com.citrix.sharefile.api.authentication.SFOAuth2Token;
import com.citrix.sharefile.api.exceptions.SFInvalidStateException;
import com.citrix.sharefile.api.exceptions.SFJsonException;
import com.citrix.sharefile.api.exceptions.SFNotAuthorizedException;
import com.citrix.sharefile.api.exceptions.SFOAuthTokenRenewException;

import java.io.IOException;

public interface ISFOAuthService
{
    public SFOAuth2Token authenticate(String subDomain, String apiControlPlane, String username, String password)
            throws SFNotAuthorizedException, SFJsonException, SFInvalidStateException;

    public SFOAuth2Token authenticate(String subDomain, String apiControlPlane, String samlAssertion)
            throws SFNotAuthorizedException, SFJsonException, SFInvalidStateException;

    public SFOAuth2Token refreshOAuthToken(SFOAuth2Token oldToken)
            throws IOException, SFOAuthTokenRenewException, SFInvalidStateException;


    public void authenticateAsync(String subDomain, String apiControlPlane, String username, String password, IOAuthTokenCallback callback);

    public void authenticateAsync(String subDomain, String apiControlPlane, String samlAssertion, IOAuthTokenCallback callback);

    public void refreshOAuthTokenAsync(SFOAuth2Token oldToken, IOAuthTokenCallback callback);
}