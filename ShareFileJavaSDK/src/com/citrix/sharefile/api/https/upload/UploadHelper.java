package com.citrix.sharefile.api.https.upload;

import com.citrix.sharefile.api.SFApiClient;
import com.citrix.sharefile.api.SFConnectionManager;
import com.citrix.sharefile.api.constants.SFKeywords;
import com.citrix.sharefile.api.https.SFCookieManager;
import com.citrix.sharefile.api.https.SFHttpsCaller;
import com.citrix.sharefile.api.log.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by sai on 2/23/17.
 */

public class UploadHelper {

    private static final String TAG = "UploadHelper";

    public static String getAppendParams(String filename, String mDetails, long fileSize,int finish,boolean isbatchLast,String hash,long index, long previousChunkTotal)
    {
        Logger.d(TAG, "ResumeSupp: Uploading chunk: index" + index + " offset: " + previousChunkTotal);
        StringBuilder sb = new StringBuilder();

        sb.append("&filehash="); sb.append(hash);
        sb.append("&finish="+ finish);
        if(isbatchLast)
        {
            sb.append("&isbatchlast=true");
        }
        sb.append("&fmt=json");
        sb.append("&hash="+hash);
        sb.append("&filesize="+fileSize);
        sb.append("&index="+index);
        sb.append("&byteOffset="+previousChunkTotal);

        if(isbatchLast && mDetails!=null && mDetails.length()>0)
        {
            try
            {
                sb.append("&details="+ URLEncoder.encode(mDetails, SFKeywords.UTF_8));
            }
            catch (UnsupportedEncodingException e)
            {
                Logger.e(TAG,e);
            }
        }

        return sb.toString();
    }

    public static String getAppendParams(String hash, long index, long byteOffset, long fileSize) {
        StringBuilder sb = new StringBuilder();

        sb.append("&filehash="); sb.append(hash);
        sb.append("&fmt=json");
        sb.append("&hash="+hash);
        sb.append("&filesize="+fileSize);
        sb.append("&index="+index);
        sb.append("&byteOffset="+byteOffset);

        return sb.toString();
    }

    public static void closeStream(Closeable fis) {
        if(fis==null) return;

        try {
            fis.close();

        } catch (IOException e)  {
            Logger.e(TAG,e);
        }
    }

    public static HttpsURLConnection getChunkUploadConnection(String finalURL, SFApiClient mApiClient, String mUsername, String mPassword, SFCookieManager mCookieManager, long chunkLength) throws Exception{
        HttpsURLConnection conn = (HttpsURLConnection) SFConnectionManager.openConnection(new URL(finalURL));

        SFHttpsCaller.addAuthenticationHeader(conn, mApiClient.getOAuthToken(), mUsername,mPassword,mCookieManager);
        conn.setUseCaches(false);
        conn.setRequestProperty(SFKeywords.CONTENT_TYPE, SFKeywords.APPLICATION_OCTET_STREAM);
        conn.setRequestProperty(SFKeywords.CONTENT_LENGTH, ""+chunkLength);
        conn.setFixedLengthStreamingMode(chunkLength);
        SFHttpsCaller.setPostMethod(conn);

        return conn;
    }
}
