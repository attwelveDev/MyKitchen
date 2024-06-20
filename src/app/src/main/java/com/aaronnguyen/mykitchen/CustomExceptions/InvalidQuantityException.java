package com.aaronnguyen.mykitchen.CustomExceptions;


import androidx.annotation.NonNull;

/**
 * When incorrect quantity is given to an item
 */
public class InvalidQuantityException extends Exception{
    public enum Code {
        NON_POSITIVE
    }
    public InvalidQuantityException(@NonNull Code violationType) {
        super(violationType.name());
    }
}
