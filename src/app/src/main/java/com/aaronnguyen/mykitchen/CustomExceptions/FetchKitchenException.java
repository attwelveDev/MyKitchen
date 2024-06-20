package com.aaronnguyen.mykitchen.CustomExceptions;

/**
 * Unsuccessfully fetching kitchen when the internet is not used.
 */
public class FetchKitchenException extends Exception{
    public FetchKitchenException() {
        super("Failed to sync data on kitchen document");
    }
}
