package com.sharefile.api.https;

import javax.net.ssl.HttpsURLConnection;

import com.sharefile.api.SFV3Error;
import com.sharefile.api.constants.SFSDK;

public abstract class TransferRunnable implements Runnable {
	public interface IProgress {
		public void bytesTransfered(long byteCount);
	};
	
	/**
	 *   This object will get filled with an errorCoode and the V3Error or valid SFOBject after the response 
	 */
	public static class Result
	{
		private int mHttpErrorCode = 0; /* not sure why we need this...it probably always match the v3error.errorcode */
		private SFV3Error mV3Error = null;			
		private long bytesTransfered = 0;
		
		public void setFields(int errorCode, SFV3Error v3Error, long downloaded)
		{
			mHttpErrorCode = errorCode;
			mV3Error = v3Error;			
			bytesTransfered = downloaded;
		}

		public SFV3Error getError() {
			return mV3Error;
		}

		public long getBytesTransfered() {
			return bytesTransfered;
		}

		public boolean isSuccess() {
			return mHttpErrorCode==HttpsURLConnection.HTTP_OK;
		}

	};
	
	protected Result createCancelResult(long bytesTransfered) {
		Result ret = new Result();
		SFV3Error v3Error = new SFV3Error(SFSDK.HTTP_ERROR_CANCELED, "Canceled");
		ret.setFields(SFSDK.HTTP_ERROR_CANCELED, v3Error, bytesTransfered);
		return ret;
	}

}
