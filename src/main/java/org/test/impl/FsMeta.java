package org.test.impl;

import org.test.MyBitSet;
import org.test.entity.INode;
import org.test.exception.StorageException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by nay on 1/3/2015.
 */
public class FsMeta {
    /**
     * Root directory of the FS
     */
    public static final int ROOT_I_NODE = 0;
    private static final int PRE_HEADER_SIZE = 12;

    /**
     * Size of the block in bytes. Should be power of 2 (?)
     */
    private int blockSize;
    /**
     * Size of the filesystem in quantity of blocks (for simplicity). Note that real file  will be greater than filesize (due to overheads).
     * Bounded by 2 << 31
     */
    private int fsSize;


    private MyBitSet freeData;
    private MyBitSet freeINode;
    INode[] iNodes;

    /**
     * Used for creation of new FS (FS.format)
     *
     * @param blockSize block size in bytes
     * @param fsSize    FS size in blocks
     */
    FsMeta(int blockSize, int fsSize) {
        this.blockSize = blockSize;
        this.fsSize = fsSize;

        //free data bit vector, one bit for each block
        freeData = new MyBitSetLv1(fsSize);
        //free iNode bit vector, we also go with one bit for each block (statistically it should be lower, but this bit vector will take small part of FS size anyway, so for now it's OK.
        freeINode = new MyBitSetLv1(fsSize);
        //initialize in-memory array of iNodes
        iNodes = new INode[fsSize];
        //init root INode
        iNodes[ROOT_I_NODE] = new INode(false, ROOT_I_NODE);
        try {
            //mark accordingly associated bit vectors for root node
            freeData.set(ROOT_I_NODE);
            freeINode.set(ROOT_I_NODE);
        } catch (StorageException e) {
            throw new RuntimeException("Programmer error. Cannot create root node.");
        }
    }

    static FsMeta valueOf(RandomAccessFile f) throws StorageException {
        try {
            f.seek(0);
            int serializedSize = f.readInt();
            //not exactly fine, perhaps should move some logic to BitSetFactory, but this should work with small overhead
            byte[] serializedForm = new byte[serializedSize];
            f.seek(0);
            f.readFully(serializedForm);
            return valueOf(serializedForm);
        } catch (IOException e) {
            throw new StorageException("Error mounting FS. Cannot read meta info.");
        }
    }

    /**
     * Used for reading existing, previously created FS
     *
     * @param serializedForm byte array with serialized FsMeta (for format see code below)
     */
    static FsMeta valueOf(byte[] serializedForm) {
        ByteBuffer buf = ByteBuffer.wrap(serializedForm);
        int sfOffset = 0;
        int serializedSize = buf.getInt();
        int fsSize = buf.getInt();
        int blockSize = buf.getInt();
        sfOffset += PRE_HEADER_SIZE;
        FsMeta res = new FsMeta(blockSize, fsSize);

        //not exactly fine, perhaps should move some logic to BitSetFactory, but this should work with small overhead
        int bitSetSize = new MyBitSetLv1(fsSize).getSerializedSize();

        byte[] bitSetBytes;

        bitSetBytes = new byte[bitSetSize];
        System.arraycopy(serializedForm, sfOffset, bitSetBytes, 0, bitSetSize);
        sfOffset += bitSetSize;
        res.freeData = new MyBitSetLv1(bitSetBytes, fsSize);

        System.arraycopy(serializedForm, sfOffset, bitSetBytes, 0, bitSetSize);
        sfOffset += bitSetSize;
        res.freeINode = new MyBitSetLv1(bitSetBytes, fsSize);

        res.iNodes = new INode[fsSize];
        byte[] iNodeBytes = new byte[INode.getSerializedSize()];
        //TODO: don't like how the iNodes are being de-serialized, especially the array comparison to determine if we must create an Object.
        for (int i = 0; i < fsSize; i++) {
            System.arraycopy(serializedForm, sfOffset, iNodeBytes, 0, INode.getSerializedSize());
            sfOffset += INode.getSerializedSize();
            if (!byteArrayEmpty(iNodeBytes)) {
                res.iNodes[i] = new INode(iNodeBytes);
            }
        }
        return res;
    }

    //actually should be reasonably fast after compiler optimization
    //we can also optimize on INode serialized data structure (no need to check all byte array, only 4 bytes are enough). This shouldn't be a bottleneck however.
    private static boolean byteArrayEmpty(byte[] bytes) {
        for (byte b : bytes) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    public int getSerializedSize() {
        return PRE_HEADER_SIZE + freeData.getSerializedSize() + freeINode.getSerializedSize() + fsSize * INode.getSerializedSize();
    }

    public byte[] toByteArray() {
        //this FsMeta serializes into: 2 ints, two BitSets w' length of fsSize and fsSize of INodes
        ByteBuffer buf = ByteBuffer.allocate(getSerializedSize());
        buf.putInt(getSerializedSize());
        buf.putInt(fsSize);
        buf.putInt(blockSize);

        buf.put(freeData.toByteArray());
        buf.put(freeINode.toByteArray());

        byte[] emptyINode = new byte[INode.getSerializedSize()];
        Arrays.fill(emptyINode, (byte) 0);
        for (INode iN : iNodes) {
            if (iN != null) {
                buf.put(iN.toByteArray());
            } else {
                buf.put(emptyINode);
            }
        }
        //if some INodes are free, they should be saved (from the data point of view) in file as an array of zeros. If it's not so, we wouldn't be able to deserialize properly.

        return buf.array();
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getFsSize() {
        return fsSize;
    }

    public MyBitSet getFreeData() {
        return freeData;
    }

    public MyBitSet getFreeINode() {
        return freeINode;
    }

    public INode[] getINodes() {
        return iNodes;
    }

    public INode getRoot() {
        return iNodes[ROOT_I_NODE];
    }

    @Override
    public String toString() {
        return "FsMeta{" +
                "blockSize=" + blockSize +
                ", fsSize=" + fsSize +
                ", freeData=" + freeData +
                ", freeINode=" + freeINode +
                ", iNodes=" + Arrays.toString(iNodes) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FsMeta fsMeta = (FsMeta) o;

        if (blockSize != fsMeta.blockSize) return false;
        if (fsSize != fsMeta.fsSize) return false;
        if (!freeData.equals(fsMeta.freeData)) return false;
        if (!freeINode.equals(fsMeta.freeINode)) return false;
        if (!Arrays.equals(iNodes, fsMeta.iNodes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = blockSize;
        result = 31 * result + fsSize;
        result = 31 * result + freeData.hashCode();
        result = 31 * result + freeINode.hashCode();
        result = 31 * result + Arrays.hashCode(iNodes);
        return result;
    }
}
