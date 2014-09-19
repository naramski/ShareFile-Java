package com.sharefile.api.exceptions;

import com.sharefile.api.SFV3Error;

import javax.net.ssl.HttpsURLConnection;

@SuppressWarnings("serial")
public class SFV3ErrorException extends SFAbstractException
{
	private final SFV3Error mV3Error;
	
	public SFV3ErrorException(SFV3Error v3error) 
	{
		super(new RuntimeException());
		mV3Error = v3error;
	}
	
	public SFV3Error getV3Error()
	{
		return mV3Error;
	}
	
	public boolean isAuthException()
	{
		boolean result = false;
		
		if(mV3Error.httpResponseCode ==  HttpsURLConnection.HTTP_UNAUTHORIZED)
		{
			result = true;
		}
		
		return result;
	}

    @Override
    public String getMessage() {
        if ( mV3Error==null ) return super.getMessage();

        StringBuilder str = new StringBuilder("http:" + String.valueOf(mV3Error.httpResponseCode));
        if ( mV3Error.code!=null && !mV3Error.code.isEmpty()) {
            str.append("; code:");
            str.append(mV3Error.code);
        }

        if ( mV3Error.mExtraInfo!=null && !mV3Error.mExtraInfo.isEmpty() ) {
            str.append("; extra:");
            str.append(mV3Error.mExtraInfo);
        }

        if ( mV3Error.message!=null ) {
            if ( mV3Error.message.lang!=null && !mV3Error.message.lang.isEmpty() ) {
                str.append("; lang:");
                str.append(mV3Error.message.lang);
            }
            if ( mV3Error.message.value!=null && !mV3Error.message.value.isEmpty() ) {
                str.append("; value:");
                str.append(mV3Error.message.value);
            }
        }

        return str.toString();
    }
}