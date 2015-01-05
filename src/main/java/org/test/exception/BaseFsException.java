package org.test.exception;

/**
 * Base Exception class for all exceptions in this FS
 * Created by nay on 1/2/2015.
 */
public class BaseFsException extends Exception {
    public BaseFsException() {
    }

    public BaseFsException(String message) {
        super(message);
    }

    public BaseFsException(String message, Throwable cause) {
        super(message, cause);
    }
}
