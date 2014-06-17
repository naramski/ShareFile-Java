package com.sharefile.api.enumerations;

/**
 *  Wheneever we get a response for folder enumerations , the SDK will attempt a deeper read if the response contains a 
 *  SymbolicLink or Redirection object. This enum helps use reduce the number of instance of checks while detecting the type
 *  of read ahead needed.
 */
public enum SFReadAheadType 
{
	READ_AHEAD_NONE,
	READ_AHEAD_SYMBOLIC_LINK,
	READ_AHEAD_REDIRECTION;		
}