package com.citrix.sharefile.api.exceptions;

public class SFSDKException extends Exception
{
	/**
	 * [auto-generated]
	 */
	private static final long serialVersionUID = 6649156220182154390L;

	public SFSDKException(Throwable ex)
	{
		super(ex);
	}

    public SFSDKException(String detailedMessage)
    {
        super(detailedMessage);
    }

    public SFSDKException(String detailedMessage, Throwable ex)
    {
        super(detailedMessage,ex);
    }
}
