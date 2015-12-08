package com.citrix.sharefile.api.models;

import com.citrix.sharefile.api.enumerations.SFSafeEnum;
import com.google.gson.annotations.SerializedName;

public class SFODataExceptionMessage {
    @SerializedName("_lang")
    private String language;

    @SerializedName("value")
    private String value;

    @SerializedName("stack")
    private String stack;

    public String getLanguage()
    {
        return language;
    }

    public String getStack()
    {
        return stack;    }

    public String getValue()
    {
        return value;
    }
}
