package com.citrix.sharefile.api.extensions;

import com.citrix.sharefile.api.entities.SFCapabilitiesEntity;
import com.citrix.sharefile.api.interfaces.ISFApiClient;
import com.citrix.sharefile.api.interfaces.ISFQuery;
import com.citrix.sharefile.api.models.SFCapability;
import com.citrix.sharefile.api.models.SFFolder;
import com.citrix.sharefile.api.models.SFItem;
import com.citrix.sharefile.api.models.SFODataFeed;
import com.citrix.sharefile.api.models.SFRedirection;
import com.citrix.sharefile.api.models.SFSymbolicLink;

import java.net.URI;
import java.net.URISyntaxException;

public class SFCapabilitiesEntityEx extends SFCapabilitiesEntity
{
    public SFCapabilitiesEntityEx(ISFApiClient client)
    {
        super(client);
    }

    public ISFQuery<SFODataFeed<SFCapability>> get(SFItem item) throws URISyntaxException
    {
        if(item instanceof SFSymbolicLink)
        {
            return get(((SFSymbolicLink) item).getLink());
        }
        else if(item instanceof SFFolder)
        {
            SFFolder folder = (SFFolder) item;

            if(folder.getRedirection()!=null)
            {
                return get(folder.getRedirection().getUri());
            }
        }

        return get(item.geturl());
    }

    public ISFQuery<SFODataFeed<SFCapability>> get(URI uri) throws URISyntaxException
    {
        ISFQuery<SFODataFeed<SFCapability>> query = super.get();

        query.setBaseLink(uri);

        return query;
    }
}
