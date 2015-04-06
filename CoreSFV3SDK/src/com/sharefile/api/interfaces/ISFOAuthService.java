package com.sharefile.api.interfaces;

import com.sharefile.api.authentication.SFOAuth2Token;
import com.sharefile.api.exceptions.SFInvalidStateException;
import com.sharefile.api.exceptions.SFJsonException;
import com.sharefile.api.exceptions.SFNotAuthorizedException;
import com.sharefile.api.exceptions.SFOAuthTokenRenewException;

import java.io.IOException;

public interface ISFOAuthService
{
    /**
     * Authenticate via username/password
     *
     * @param subDomain
     *            - hostname like "yourcompanyname"
     * @param apiControlPlane
     *            - hostname like "sharefile.com"
     * @param username
     *            - my@user.name
     * @param password
     *            - mypassword
     * @return an OAuth2Token instance
     * @throws SFJsonException
     */
    public SFOAuth2Token authenticate(String subDomain, String apiControlPlane, String username, String password)
            throws SFNotAuthorizedException, SFJsonException, SFInvalidStateException;

    /**
     * Authenticate via samlAssertion
     *
     * @param subDomain
     *            - hostname like "yourcompanyname"
     * @param apiControlPlane
     *            - hostname like "sharefile.com"
     * @param samlAssertion
     *            - Base64 URL encoded SAML assertion.
     * @return an OAuth2Token instance
     * @throws SFJsonException
     */
    public SFOAuth2Token authenticate(String subDomain, String apiControlPlane, String samlAssertion)
            throws SFNotAuthorizedException, SFJsonException, SFInvalidStateException;

    public SFOAuth2Token refreshOAuthToken(SFOAuth2Token oldToken)
            throws IOException, SFOAuthTokenRenewException, SFInvalidStateException;
}