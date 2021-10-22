package com.feedbeforeflight.marshrutka.services;

public class TransferException extends RuntimeException{

    public TransferException(String message) {
        super(message);
    }

    public TransferException(Throwable cause) {
        super(cause);
    }

    public TransferException(String message, Throwable cause) {
        super(message, cause);
    }

}
