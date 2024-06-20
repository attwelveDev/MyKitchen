package com.aaronnguyen.mykitchen.CustomExceptions;

/**
 * Unable to join the kitchen for certain problems
 */
public class JoinKitchenException extends Exception {
    public enum Code {
        BANNED,
        ALREADY_REQUESTED,
        ALREADY_JOINED
    }

    private final Code code;

    public JoinKitchenException(Code code) {
        super();

        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
