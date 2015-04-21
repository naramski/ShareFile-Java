package com.citrix.sharefile.api.interfaces;


import com.citrix.sharefile.api.exceptions.SFSDKException;
import com.citrix.sharefile.api.models.SFODataObject;
import com.citrix.sharefile.api.utils.Utils;

/**
 *  Helper class for the app to implement internal listeners
 * @param <T>
 * @param <T>
 */
public abstract class ISFApiResultResponseTranslator<T extends SFODataObject, T2 extends SFODataObject> implements ISFApiResultCallback<T>
{
	private ISFApiResultCallback<T2> mListener;

	public ISFApiResultResponseTranslator(ISFApiResultCallback<T2> listener)
	{
		mListener = listener;
	}

    public ISFApiResultResponseTranslator()
    {

    }

	public void setListener(ISFApiResultCallback<T2> listener)
	{
		mListener = listener;
	}

    public abstract T2 translate(T sfobject);

    @Override
    public void onSuccess(T object)
    {
        Utils.safeCallSuccess(mListener,translate(object));
    }

    @Override
    public void onError(SFSDKException exception, ISFQuery query)
    {
        Utils.safeCallErrorListener(mListener,exception,query);
    }
}