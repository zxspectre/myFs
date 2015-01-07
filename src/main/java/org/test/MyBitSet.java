package org.test;

import org.test.exception.StorageException;

/**
 * Created by nay on 1/5/2015.
 */
public interface MyBitSet {
    int nextClear(int startFrom) throws StorageException;
    void clear(int pos) throws StorageException;
    void set(int pos) throws StorageException;
    byte[] toByteArray();
    int getSerializedSize();
}
