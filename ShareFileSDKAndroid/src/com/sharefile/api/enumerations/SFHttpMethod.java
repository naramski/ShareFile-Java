package com.sharefile.api.enumerations;

public enum SFHttpMethod 
{
	GET{@Override public String toString() {return "GET";}},
	POST{@Override public String toString() {return "POST";}},
	PUT{@Override public String toString() {return "PUT";}},
	PATCH{@Override public String toString() {return "PATCH";}},
	DELETE{@Override public String toString() {return "DELETE";}},
}