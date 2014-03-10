package com.sharefile.api.android.utils;

import java.util.concurrent.Executor;

import android.os.AsyncTask;

public class SFAsyncTask
{
//	public final AsyncTask<Params, Progress, Result> executeSF(Params... params)
//	{
//		if(Build.VERSION.SDK_INT >= 11)
//		{
//		    return executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
//		}
//	    else
//	    {
//	    	return execute(params);
//	    }
//	}
	public static final Executor DefaultExecutor = AsyncTask.THREAD_POOL_EXECUTOR;
	
	@SuppressWarnings("unchecked")
	public static <T1,T2, T3> void execute(AsyncTask<T1,T2,T3> task, Object... params) 
	{
		task.executeOnExecutor(DefaultExecutor, (T1[]) params);
	}
}
