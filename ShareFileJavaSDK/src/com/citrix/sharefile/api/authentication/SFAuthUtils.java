package com.citrix.sharefile.api.authentication;

import com.citrix.sharefile.api.constants.SFKeywords;

import org.apache.http.NameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class SFAuthUtils
{
    public static final String buildWebLoginTokenUrl(String controlplane,String subdomain)
    {
        String strDot = controlplane.startsWith(".")?"":".";

        return  "https://"+subdomain+strDot+controlplane+ SFKeywords.SF_OAUTH_TOKEN_PATH;
    }

    public static final String getBodyForWebLogin(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                result.append("&");
            }

            result.append(pair.getName());
            result.append("=");
            result.append(pair.getValue());
        }

        return result.toString();
    }
}
