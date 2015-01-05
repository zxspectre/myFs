package org.test.model;

import org.test.MyBitSet;

/**
 * Created by nay on 1/3/2015.
 */
public class FsMeta {
    /**
     * Size of the block in bytes. Should be power of 2 (?)
     */
    private int blockSize;
    /**
     * Size of the filesystem in quantity of blocks (for simplicity). Note that real file  will be greater than filesize (due to overheads).
     * Bounded by 2 << 31
     */
    private int fsSize;

    /**
     * Root directory of the FS
     */
    private INode root;

    private MyBitSet freeSpace;

    public FsMeta() {
        blockSize = 2 << 12;
        fsSize = 2 << 10;
        root = new INode(false);
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getFsSize() {
        return fsSize;
    }

    public INode getRoot() {
        return root;
    }
}
