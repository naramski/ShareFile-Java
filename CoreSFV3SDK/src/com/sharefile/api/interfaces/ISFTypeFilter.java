package com.sharefile.api.interfaces;

import com.sharefile.api.enumerations.SFV3ElementType;

public interface ISFTypeFilter<T>
{
    ISFQuery<T> filter(String filterValue);

    ISFQuery and(SFV3ElementType type);

    ISFQuery or(SFV3ElementType type);

    ISFQuery is(SFV3ElementType type);
}