package com.wizard.btms.exception;

public class UnauthorizedAccountAccessException
        extends RuntimeException {

    public UnauthorizedAccountAccessException(String message) {
        super(message);
    }
}