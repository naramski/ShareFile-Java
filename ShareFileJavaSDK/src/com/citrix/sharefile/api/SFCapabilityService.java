package com.citrix.sharefile.api;

import com.citrix.sharefile.api.exceptions.SFSDKException;
import com.citrix.sharefile.api.interfaces.ISFApiClient;
import com.citrix.sharefile.api.interfaces.ISFCapabilityService;
import com.citrix.sharefile.api.interfaces.ISFQuery;
import com.citrix.sharefile.api.log.Logger;
import com.citrix.sharefile.api.models.SFCapability;
import com.citrix.sharefile.api.models.SFCapabilityName;
import com.citrix.sharefile.api.models.SFODataFeed;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Wes on 12/16/15.
 */
public class SFCapabilityService implements ISFCapabilityService
{
    private static SFCapabilityService instance;

    public static ISFCapabilityService get()
    {
        if(instance == null) instance = new SFCapabilityService();
        return instance;
    }

    protected static SFCapabilityService getInternal()
    {
        if(instance == null) instance = new SFCapabilityService();
        return instance;
    }

    private HashMap<URI, SFODataFeed<SFCapability>> mUSerCapabilities = new HashMap<>();

    public void getCapabilities(String providerUri, ISFApiClient client) {
        URI uriKey = getProviderUri(providerUri);
        if(mUSerCapabilities.containsKey(providerUri))return;

        //Since it doesn't exist, add it initially with a null entry to avoid an infinite loop.
        mUSerCapabilities.put(uriKey,null);
        try
        {
            SFODataFeed<SFCapability> capabilities = client.executeQuery(getQuery(uriKey,client));
            mUSerCapabilities.put(uriKey,capabilities);
        }
        catch(SFSDKException ex)
        {
            Logger.e(getClass().getSimpleName(), ex);
            mUSerCapabilities.remove(providerUri);
        }
    }

    @Override
    public boolean hasCapability(String anyUri, SFCapabilityName capability) {
        URI uriKey = getProviderUri(anyUri);
        SFODataFeed<SFCapability> capabilities = mUSerCapabilities.get(uriKey);

        if (capabilities != null)
        {
            ArrayList<SFCapability> capabilitiesList = capabilities.getFeed();
            for (SFCapability c : capabilitiesList) {
                if (c.getName().equals(capability)) {
                    return true;
                }

            }
        }
        else
        {
            Logger.d(getClass().getSimpleName(), "Couldn't find capabilities for : " + anyUri.toString());
        }
        return false;
    }

    @Override
    public boolean providerCapabilitiesLoaded(String anyUri) {
        URI uriKey = getProviderUri(anyUri);
        return mUSerCapabilities.containsKey(uriKey);
    }

    private URI getProviderUri(String queryUrl)
    {
        URI uri = URI.create(queryUrl);
        String provider = SFProvider.getProviderType(queryUrl);
        String hostAndProvider = "https://" + uri.getHost() + "/" + provider + "/v3";
        return URI.create(hostAndProvider);
    }

    private ISFQuery<SFODataFeed<SFCapability>>  getQuery(URI folderUri, ISFApiClient client) throws SFSDKException {
        try
        {
            ISFQuery<SFODataFeed<SFCapability>> query =  client.capabilities().get();
            query.setBaseLink(getProviderUri(folderUri.toString()));
            return query;
        }
        catch (URISyntaxException e)
        {
            Logger.e(getClass().getSimpleName(), e);
            throw new SFSDKException(e);
        }
    }
}
