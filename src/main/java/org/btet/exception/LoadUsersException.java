package org.btet.exception;
/**
 * Exception that is thrown when there is a problem with loading users from the file
 */
public class LoadUsersException extends Exception{
    public LoadUsersException() {
    }

    public LoadUsersException(String message) {
        super(message);
    }

    public LoadUsersException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoadUsersException(Throwable cause) {
        super(cause);
    }

    public LoadUsersException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
