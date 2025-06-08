package org.btet.exception;
/**
 * Exception thrown when the user tries to login with invalid credentials.
 * */
public class InvalidLoginCredentialsException extends RuntimeException {
    public InvalidLoginCredentialsException(String message) {
        super(message);
    }

    public InvalidLoginCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidLoginCredentialsException(Throwable cause) {
        super(cause);
    }

    public InvalidLoginCredentialsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InvalidLoginCredentialsException() {
    }
}
