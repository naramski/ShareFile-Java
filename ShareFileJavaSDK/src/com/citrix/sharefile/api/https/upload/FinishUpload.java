package com.citrix.sharefile.api.https.upload;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by sai on 2/16/17.
 */

/*
    Errors messages have json like:

    {
      "error":true,
     "errorMessage":"Server Error String",
     "errorCode":500
     }

   Success message has json like:

      {
         "error":false,
        "value":
        [
            {
             "uploadid":"rsu-xyz",
             "parentid":"fo2xyz",
             "id":"fixyz",
             "filename":"2.txt",
             "displayname":"2.txt",
             "size":27056777,
             "md5":"55e4be1382b662e76555f6714cf5a468"
             }
        ]
      }

 */
public class FinishUpload {

    @SerializedName("error")
    public Boolean error;

    @SerializedName("errorMessage")
    public String errorMessage;

    @SerializedName("value")
    public List<UploadValue> valueList;

    public String getError() {
        return errorMessage;
    }

    public void setError(String error) {
        this.errorMessage = error;
    }

    public List<UploadValue> getValueList() {
        return valueList;
    }

    public void setValueList(List<UploadValue> valueList) {
        this.valueList = valueList;
    }

    class UploadValue {
        @SerializedName("uploadid")
        public String uploadId;

        @SerializedName("parentid")
        public String parentId;

        @SerializedName("id")
        public String itemId;

        @SerializedName("filename")
        public String fileName;

        @SerializedName("displayname")
        public String displayName;

        @SerializedName("size")
        public long size;

        @SerializedName("md5")
        public String hash;


        public long getSize() {
            return size;
        }

        public String getItemId() {
            return itemId;
        }
    }
}
