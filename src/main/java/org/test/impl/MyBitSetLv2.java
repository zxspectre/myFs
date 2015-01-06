package org.test.impl;

import org.test.MyBitSet;

import java.util.BitSet;

/**
 * Bitset with 2-level structure for increased performance.
 * Created by nay on 1/5/2015.
 */
public class MyBitSetLv2 implements MyBitSet {
    /**
     * TODO: sync
     * will be 1-thread synced on this bitmap to get (mark them as 1) a bunch of free sectors (parametrized value 'preWriteSectors'),
     * use them, if need moer - sync again and get next bunch. If file write completed and some free sectors left, sync and mark them as 0.
     */
    private BitSet freeSpaceLv1;

    private BitSet freeSpaceLv2;

    public MyBitSetLv2(int fsBlockCnt) {
        int lv2Size = (int) Math.floor(Math.sqrt(fsBlockCnt));
        this.freeSpaceLv1 = new BitSet(fsBlockCnt);
        this.freeSpaceLv2 = new BitSet(lv2Size);
        freeSpaceLv1.toByteArray();
    }


    @Override
    public int nextClear(int startFrom) {
        return 0;
    }

    @Override
    public void clear(int pos) {

    }

    @Override
    public void set(int pos) {

    }

    @Override
    public byte[] toByteArray() {
        return new byte[0];
    }

    @Override
    public int getSerializedSize() {
        return 0;
    }
}
