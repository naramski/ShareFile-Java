package com.citrix.sharefile.api;

import com.citrix.sharefile.api.interfaces.ISFApiClient;

import java.io.InputStream;

/**
 * Created by tarungo on 1/28/2016.
 */
class AbstractSFApiQueryFactory {

    public static <V> AbstractSFApiQuery getAbstractSFApiQuery(Class<V> clazz, ISFApiClient apiClient)
    {
        AbstractSFApiQuery newQuery = null;

        if(InputStream.class.isAssignableFrom(clazz) )
        {
            newQuery = new SFQueryStream(apiClient);
        }
        else
        {
            newQuery = new SFApiQuery(apiClient);
        }
        return newQuery;
    }
}
