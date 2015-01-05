package org.test;

import org.junit.Assert;
import org.junit.Test;
import org.test.model.INode2;

/**
 * Created by nay on 1/6/2015.
 */
public class NodeSerializationTest {
    @Test
    public void testSer() throws Exception{
        INode2 inode = new INode2();
        inode.blocks = 4;
        inode.size = 3216748;
        inode.nextChunk = 0;
        inode.data[4] = 17;
        inode.isFile = 1;

        byte[] ser = inode.toByteArray();
        INode2 node2 = new INode2(ser);

        Assert.assertEquals(inode, node2);
    }


    @Test
    public void testSer2() throws Exception{
        INode2 inode = new INode2();
        inode.blocks = 100000;
        inode.size = 3216748;
        inode.nextChunk = 100001;
        inode.data[0] = 170000;
        inode.data[1] = 160000;
        inode.data[8] = 160001;
        inode.data[9] = 170002;
        inode.isFile = 0;

        byte[] ser = inode.toByteArray();
        INode2 node2 = new INode2(ser);

        Assert.assertEquals(inode, node2);
    }
}
