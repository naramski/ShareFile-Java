package com.sharefile.api.exceptions;

import com.sharefile.api.SFV3Error;

public class ReAuthException extends SFV3ErrorException{
    public ReAuthException(SFV3Error error) {
        super(error);
    }
}
