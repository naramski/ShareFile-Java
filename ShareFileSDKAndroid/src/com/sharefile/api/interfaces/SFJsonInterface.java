package com.sharefile.api.interfaces;

import com.sharefile.api.exceptions.SFJsonException;

/**   
 * To keep the Selection of JSON library/implemetation independent of our core SDK we make all the jsonizable objects
 * implement a generic interface. Any class in the ShareFile Android Client SDK that needs to expose JSON serialize/deserialise
 * functionality should implement this interface instead of providing raw functions.
 */
public interface SFJsonInterface 
{
	/**
	 *   serialize a given object to its json string representation.
	 */
	public String toJsonString() throws SFJsonException;
	
	/**
	 *  de-seriaize a string into a Java Object 
	 */
	public void parseFromJson(String jsonString ) throws SFJsonException;	
}