package com.wizard.btms.exception;

public class DailyTransferLimitExceededException
        extends RuntimeException {

    public DailyTransferLimitExceededException(
            String message
    ) {
        super(message);
    }
}