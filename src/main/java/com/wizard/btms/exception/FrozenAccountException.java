package com.wizard.btms.exception;

public class FrozenAccountException
        extends RuntimeException {

    public FrozenAccountException(
            String message
    ) {
        super(message);
    }
}