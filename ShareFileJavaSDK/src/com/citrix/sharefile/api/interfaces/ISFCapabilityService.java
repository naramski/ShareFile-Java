package com.citrix.sharefile.api.interfaces;
import com.citrix.sharefile.api.exceptions.SFSDKException;
import com.citrix.sharefile.api.models.SFCapabilityName;

import java.net.URI;

/**
 * Created by Wes on 12/16/15.
 */
public interface ISFCapabilityService {
    boolean hasCapability(String anyUri, SFCapabilityName capability);
    boolean providerCapabilitiesLoaded(String anyUri);
}
