package com.qa.atlibs.exception;

public class GrpcTestException extends RuntimeException {

    public GrpcTestException(String message, Throwable cause) {
        super(message, cause);
    }

    public GrpcTestException(String message) {
        super(message);
    }

    public GrpcTestException(Throwable cause) {
        super(cause);
    }

}
