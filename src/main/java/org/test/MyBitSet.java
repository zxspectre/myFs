package org.test;

/**
 * Created by nay on 1/5/2015.
 */
public interface MyBitSet {
    int nextClear(int startFrom);
    void clear(int pos);
    void set(int pos);
    byte[] toByteArray();
    int getSerializedSize();
}
