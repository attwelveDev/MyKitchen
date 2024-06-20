package com.aaronnguyen.mykitchen.DAO;

public interface WriteListener {
    <T> void onWriteSuccess(T data);
    void onWriteFailure(Exception exception);
}
