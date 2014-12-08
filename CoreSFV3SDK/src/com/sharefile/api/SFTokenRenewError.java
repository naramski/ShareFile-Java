package com.sharefile.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sharefile.api.constants.SFKeywords;
import com.sharefile.api.gson.SFGsonHelper;

/**
 * //Token refresh errors are different json format than the regular V3 errors
 * {"error":"invalid_grant","error_description":"refresh token expired"}
 */
public class SFTokenRenewError extends SFV3Error
{
    public SFTokenRenewError(int serverHttpCode, String serverRespSring, Exception exception)
    {
        super(serverHttpCode,exception);

        if(serverRespSring == null)
        {
            mServerResponse.value = super.getErrorMessageFromErroCode(serverHttpCode);
            return;
        }

        try
        {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(serverRespSring);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            mServerResponse.code = SFGsonHelper.getString(jsonObject, "error", "");
            mServerResponse.value = SFGsonHelper.getString(jsonObject, "error_description", "");
        }
        catch (Exception e)
        {
            mInternalException = exception;
        }
    }

    @Override
    public String errorDisplayString(String optionalLocalized)
    {
        return "OAuth token renew failed";
    }
}
