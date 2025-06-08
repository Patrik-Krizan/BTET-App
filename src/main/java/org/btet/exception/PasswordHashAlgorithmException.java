package org.btet.exception;
/**
 * Exception that is thrown when there is a problem with hashing the password using the algorithm
 */
public class PasswordHashAlgorithmException extends Exception {
    public PasswordHashAlgorithmException(String message) {
        super(message);
    }

    public PasswordHashAlgorithmException(String message, Throwable cause) {
        super(message, cause);
    }

    public PasswordHashAlgorithmException(Throwable cause) {
        super(cause);
    }

    public PasswordHashAlgorithmException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public PasswordHashAlgorithmException() {
    }
}
