package com.sharefile.api.interfaces;

import com.sharefile.api.SFV3Error;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.utils.Utils;

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
    public void onError(SFV3Error error, ISFQuery query)
    {
        Utils.safeCallErrorListener(mListener,error,query);
    }
}