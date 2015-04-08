package com.sharefile.api.interfaces;

public interface ISFAsyncHelper<T>
{
    public T execute();
    public void onPostExecute();
}
