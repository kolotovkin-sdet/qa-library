package com.qa.atlibs.exception;

public class CoreTestException extends RuntimeException {
	public CoreTestException(String message, Throwable cause) {
		super(message, cause);
	}

	public CoreTestException(String message) {
		super(message);
	}

	public CoreTestException(Throwable cause) {
		super(cause);
	}
}
