package com.sharefile.api;

import com.sharefile.api.interfaces.ISFQuery;
import com.sharefile.api.interfaces.SFApiResponseListener;
import com.sharefile.api.models.SFODataObject;
import com.sharefile.api.utils.Utils;

/**
 *  Helper class for the app to implement internal listeners
 * @param <T>
 * @param <T>
 */
public abstract class SFApiResponseTranslator<T extends SFODataObject, T2 extends SFODataObject> implements SFApiResponseListener<T>
{
	private SFApiResponseListener<T2>  mListener;

	public SFApiResponseTranslator(SFApiResponseListener<T2> listener)
	{
		mListener = listener;
	}

    public SFApiResponseTranslator()
    {

    }

	public void setListener(SFApiResponseListener<T2>  listener)
	{
		mListener = listener;
	}

    public abstract T2 translate(T sfobject);

    @Override
    public void sfApiSuccess(T object)
    {
        Utils.safeCallSuccess(mListener,translate(object));
    }

    @Override
    public void sfApiError(SFV3Error error, ISFQuery query)
    {
        Utils.safeCallErrorListener(mListener,error,query);
    }
}