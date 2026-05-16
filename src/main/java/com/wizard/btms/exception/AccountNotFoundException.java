package com.wizard.btms.exception;

public class AccountNotFoundException
        extends RuntimeException {

    public AccountNotFoundException(String message) {
        super(message);
    }
}