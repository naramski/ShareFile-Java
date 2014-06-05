package com.sharefile.testv3;

import com.sharefile.api.models.SFFile;

public class SFFileEx extends SFFile 
{
	private boolean mIsSynced = false;
	
	public boolean isFileSynced()
	{
		return mIsSynced;
	}
	
	public void setSyncState(boolean val)
	{
		mIsSynced = val;
	}
}
