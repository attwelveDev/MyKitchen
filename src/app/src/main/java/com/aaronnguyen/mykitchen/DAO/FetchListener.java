package com.aaronnguyen.mykitchen.DAO;

/**
 * This is the listener / callback when firebase fetch is finished
 */
public interface FetchListener {
    <T> void onFetchSuccess(T data);
    void onFetchFailure(Exception exception);
}
