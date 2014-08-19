package com.sharefile.api.enumerations;

/**
 *  Whenever we get a response for folder enumerations , the SDK will attempt a deeper read if the response contains a 
 *  SymbolicLink or Redirection object. This enum helps use reduce the number of instance of checks while detecting the type
 *  of read ahead needed.
 */
public enum SFRedirectionType 
{
	/** This implies that the result we got from server was full and final and is supposed to be passed to the application*/
 	NONE,   
 	
 	/** This implies we got symbolic links (personal connectors return this) and need to reexecute the orignal query on the contained URI*/
 	SYMBOLIC_LINK,
	
	/** This implies we got SFRedirection (ZK folders do this) and need to reexecute the orignal query on the contained URI*/
 	URI,
	
	/** This implies that the SFRedirection was contained inside a folder enumeration and needs to requeries on the contained URI*/
 	FOLDER_ENUM  		
	; 		
}