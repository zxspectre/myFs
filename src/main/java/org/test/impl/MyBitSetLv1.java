package org.test.impl;

import org.test.MyBitSet;

import java.util.BitSet;

/**
 * Created by nay on 1/5/2015.
 */
public class MyBitSetLv1 implements MyBitSet {

    private BitSet freeSpaceLv1;

    public MyBitSetLv1(int fsBlockCnt) {
        this.freeSpaceLv1 = new BitSet(fsBlockCnt);
    }

    @Override
    public int nextClear(int startFrom) {
        return freeSpaceLv1.nextClearBit(startFrom);
    }

    @Override
    public void clear(int pos) {
        freeSpaceLv1.clear(pos);
    }

    @Override
    public void set(int pos) {
        freeSpaceLv1.set(pos);
    }
}
