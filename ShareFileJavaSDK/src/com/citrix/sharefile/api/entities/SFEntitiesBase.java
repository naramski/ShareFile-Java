package com.citrix.sharefile.api.entities;

import com.citrix.sharefile.api.interfaces.ISFApiClient;

import java.lang.reflect.InvocationTargetException;

public class SFEntitiesBase
{
    protected final ISFApiClient client;

    public SFEntitiesBase(ISFApiClient client)
    {
        this.client = client;
    }

    public SFEntitiesBase()
    {
        this.client = null;
    }

    public SFEntitiesBase getEntity(Class className)
    {
        try
        {
            if(this instanceof ISFApiClient)
            {
                return (SFEntitiesBase) className.getConstructor(ISFApiClient.class).newInstance(this);
            }
            else
            {
                return (SFEntitiesBase) className.newInstance();
            }
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }
}