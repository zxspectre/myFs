package org.test.exception;

/**
 * Thrown when no data blocks available in FS
 * Created by nay on 1/3/2015.
 */
public class OutOfFileException extends StorageException {
    public OutOfFileException() {
    }

    public OutOfFileException(String message) {
        super(message);
    }

    public OutOfFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
