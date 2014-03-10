package com.sharefile.api.enumerations;

public enum SFProvider 
{
	PROVIDER_TYPE_SF{@Override public String toString() {return "sf";}},
	PROVIDER_TYPE_CIFS{@Override public String toString() {return "cifs";}},
	PROVIDER_TYPE_SHAREPOINT{@Override public String toString() {return "sp";}}	
}