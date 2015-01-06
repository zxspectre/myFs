package org.test.impl;

import org.test.MyBitSet;

import java.util.BitSet;

/**
 * Standard BitSet with fine granularity
 * Created by nay on 1/5/2015.
 */
public class MyBitSetLv1 implements MyBitSet {

    private BitSet freeSpaceLv1;
    private int fsBlockCnt;

    public MyBitSetLv1(int fsBlockCnt) {
        this.freeSpaceLv1 = new BitSet(fsBlockCnt);
        this.fsBlockCnt = fsBlockCnt;
    }


    public MyBitSetLv1(byte[] serializedForm, int fsBlockCnt){
        //we can infer this from byte array, but for robustness get it explicitly (because we can)
        this.fsBlockCnt = fsBlockCnt;
        this.freeSpaceLv1 = BitSet.valueOf(serializedForm);
    }

    @Override
    public int nextClear(int startFrom) {
//        System.out.println("Perform range check. bitset is dynamical");
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

    @Override
    public byte[] toByteArray() {
        byte[] bytes = freeSpaceLv1.toByteArray();
        byte[] res = new byte[getSerializedSize()];
        //pad serialized bitset with zeros, because we want fixed length serialized form of bitset
        System.arraycopy(bytes, 0, res, 0, bytes.length);
        return res;
    }

    @Override
    public int getSerializedSize() {
        return (fsBlockCnt + 7) / 8;
    }

    @Override
    public String toString() {
        return "MyBitSetLv1{" +
                "freeSpaceLv1=" + freeSpaceLv1 +
                ", fsBlockCnt=" + fsBlockCnt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyBitSetLv1 that = (MyBitSetLv1) o;

        if (fsBlockCnt != that.fsBlockCnt) return false;
        if (!freeSpaceLv1.equals(that.freeSpaceLv1)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = freeSpaceLv1.hashCode();
        result = 31 * result + fsBlockCnt;
        return result;
    }
}
