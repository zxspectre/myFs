package org.test.impl;

import org.junit.Assert;
import org.junit.Test;
import org.test.entity.DirData;
import org.test.entity.INode;

import java.nio.ByteBuffer;

/**
 * Created by nay on 1/6/2015.
 */
public class SerializationTest {
    @Test
    public void testINodeSer() throws Exception {
        INode inode = new INode();
        inode.blocks = 4;
        inode.size = 3216748;
        inode.nextChunk = 0;
        inode.data[4] = 17;
        inode.isFile = 1;

        byte[] ser = inode.toByteArray();
        INode node2 = new INode(ser);

        Assert.assertEquals(inode, node2);
    }


    @Test
    public void testINodeSer2() throws Exception {
        INode inode = new INode();
        inode.blocks = 100000;
        inode.size = 3216748;
        inode.nextChunk = 100001;
        inode.data[0] = 170000;
        inode.data[1] = 160000;
        inode.data[8] = 160001;
        inode.data[9] = 170002;
        inode.isFile = 0;

        byte[] ser = inode.toByteArray();
        INode node2 = new INode(ser);

        Assert.assertEquals(inode, node2);
    }

    @Test
    public void testDirDataEntrySer() throws Exception {
        DirData.DirDataEntry dde = new DirData.DirDataEntry();
        dde.inode = 17;
        dde.name = "a";

        byte[] ser = dde.serializeData();
        ByteBuffer buf = ByteBuffer.wrap(dde.serializeHeader());
        Assert.assertEquals("dde header bytes do not correlate with dde serialized data bytes", buf.getInt(), ser.length);
        DirData.DirDataEntry dde2 = new DirData.DirDataEntry(ser);

        Assert.assertEquals(dde, dde2);
    }

    @Test
    public void testDirDataEntrySer2() throws Exception {
        DirData.DirDataEntry dde = new DirData.DirDataEntry();
        dde.inode = 70000;
        dde.name = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789()_";

        byte[] ser = dde.serializeData();
        ByteBuffer buf = ByteBuffer.wrap(dde.serializeHeader());
        Assert.assertEquals("dde header bytes do not correlate with dde serialized data bytes", buf.getInt(), ser.length);
        DirData.DirDataEntry dde2 = new DirData.DirDataEntry(ser);

        Assert.assertEquals(dde, dde2);
    }


    @Test
    public void testDirDataSer() throws Exception {
        DirData dd = new DirData();
        DirData.DirDataEntry dde = new DirData.DirDataEntry();
        dde.inode = 17;
        dde.name = "a";
        dd.children.add(dde);

        dde = new DirData.DirDataEntry();
        dde.inode = 70000;
        dde.name = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789()_";
        dd.children.add(dde);

        byte[] ser = dd.toByteArray();
        DirData dd2 = new DirData(ser);

        Assert.assertEquals(dd, dd2);

        dd.children.get(0).name = "B";
        Assert.assertNotEquals(dd, dd2);

    }


    @Test
    public void testDirDataSerTrailingZeros() throws Exception {
        DirData dd = new DirData();
        DirData.DirDataEntry dde = new DirData.DirDataEntry();
        dde.inode = 17;
        dde.name = "a";
        dd.children.add(dde);

        dde = new DirData.DirDataEntry();
        dde.inode = 70000;
        dde.name = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789()_";
        dd.children.add(dde);

        byte[] ser = dd.toByteArray();
        byte[] serTrailingZeros = new byte[ser.length + 1000];
        System.arraycopy(ser, 0, serTrailingZeros, 0, ser.length);
        DirData dd2 = new DirData(serTrailingZeros);

        Assert.assertEquals(dd, dd2);

        dd.children.get(0).name = "B";
        Assert.assertNotEquals(dd, dd2);

    }


    @Test
    public void testDirDataSerEmptyDir() throws Exception {
        DirData dd = new DirData();
        byte[] ser = dd.toByteArray();

        System.arraycopy(ser, 0, ser, 0, ser.length);
        DirData dd2 = new DirData(ser);

        Assert.assertEquals(dd, dd2);
    }


    @Test
    public void testFsMetaSer() throws Exception {
        int blockSize = 1 << 12;
        int blockCnt = 1 << 12;
        FsMeta fsMeta = new FsMeta(blockSize, blockCnt);
        byte[] ser = fsMeta.toByteArray();

        FsMeta fsMeta2 = FsMeta.valueOf(ser);

        Assert.assertEquals(fsMeta, fsMeta2);
    }

    @Test
    public void testFsMetaSerFragmentedData() throws Exception {
        int blockSize = 1 << 12;
        int blockCnt = 1 << 12;
        FsMeta fsMeta = new FsMeta(blockSize, blockCnt);
        fsMeta.getINodes()[1 << 6] = new INode(true, 1 << 6);
        fsMeta.getFreeData().set(1<<6);
        fsMeta.getFreeINode().set(1<<6);
        byte[] ser = fsMeta.toByteArray();

        FsMeta fsMeta2 = FsMeta.valueOf(ser);

        Assert.assertEquals(fsMeta, fsMeta2);
    }
}
