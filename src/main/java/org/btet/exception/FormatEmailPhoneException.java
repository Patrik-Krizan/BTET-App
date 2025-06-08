package org.btet.exception;
/**
 * Exception thrown when email or phone number is not in the correct format
 * */
public class FormatEmailPhoneException extends RuntimeException {
    public FormatEmailPhoneException(String message) {
        super(message);
    }

  public FormatEmailPhoneException(String message, Throwable cause) {
    super(message, cause);
  }

  public FormatEmailPhoneException(Throwable cause) {
    super(cause);
  }

  public FormatEmailPhoneException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public FormatEmailPhoneException() {
  }
}
