package com.sharefile.api.interfaces;

public interface IAsyncTask<Params, Progress, Result> {
    boolean cancel();
	boolean isCancelled();
	void execute(Params...params);
}