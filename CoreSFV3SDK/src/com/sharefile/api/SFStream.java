package com.sharefile.api;

import com.sharefile.api.models.SFODataObject;

import java.io.InputStream;

/**
  This is a special model which encapsulates streams for api calls which should not
  read and parse the
 */
class SFStream extends SFODataObject
{
   public InputStream inputStream;
}
