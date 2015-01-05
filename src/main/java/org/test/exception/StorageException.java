package org.test.exception;

/**
 * Exception for cases when we have problems with our 'hardware' i.e. some problems with operations for the File that stores all FS info. <br>
 * Most likely this will be IOExceptions on 'our' side, contrary to IOExceptions for external IO streams.
 * Created by nay on 1/2/2015.
 */
public class StorageException extends BaseFsException {
    public StorageException() {
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
