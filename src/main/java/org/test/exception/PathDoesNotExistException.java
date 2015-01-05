package org.test.exception;

/**
 * Created by nay on 1/2/2015.
 */
public class PathDoesNotExistException extends BaseFsException {
    public PathDoesNotExistException() {
    }

    public PathDoesNotExistException(String message) {
        super(message);
    }

    public PathDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
