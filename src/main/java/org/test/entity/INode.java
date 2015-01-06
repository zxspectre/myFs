package org.test.entity;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by nay on 1/6/2015.
 */
public class INode implements Serializable {
    public static final int DIRECT_POINTERS_SIZE = 10;

    //1 for file, 0 for dir
    public byte isFile;
    //data size in bytes (relevant for files, directory data structure allows tailing bytes).
    public int size;
    //how much data blocks assigned to this node
    public int blocks;
    //first batch of pointers to data blocks
    public int[] data = new int[DIRECT_POINTERS_SIZE];
    //if 'data' pointers are not sufficient, this 'nextChunk' will point to another iNode (by its index) whose 'data' pointer will be used to index additional data blocks
    public int nextChunk;

    public INode() {

    }

    /**
     * Shorthand constructor for newly allocated INodes (either file or dir) with no content.
     *
     * @param isFile      file or dir?
     * @param dataPointer first data block assigned to this INode by default (each INode has at least one data block associated with it).
     */
    public INode(boolean isFile, int dataPointer) {
        if(isFile){
            this.isFile = 1;
            //empty file has no data in data block whatsoever
            size = 0;
        }else{
            this.isFile = 0;
            //empty dir has at least one header entry, which tells the length of next entry length (which should be 0 in this case).
            size = DirData.DDE_HEADER_SIZE;
        }
        //by default we have one block assigned for newly created dir/file
        blocks = 1;
        //save pointer to that data block
        data[0] = dataPointer;
        //no indirect pointer (no additional data blocks required)
        nextChunk = 0;
    }

    public static int getSerializedSize() {
        return 13 + DIRECT_POINTERS_SIZE * 4;
    }

    public boolean isFile() {
        return isFile != 0;
    }

    public INode(byte[] serializedForm) {
        ByteBuffer buf = ByteBuffer.wrap(serializedForm);
        isFile = buf.get();
        size = buf.getInt();
        blocks = buf.getInt();
        for (int i = 0; i < DIRECT_POINTERS_SIZE; i++) {
            data[i] = buf.getInt();
        }
        nextChunk = buf.getInt();
        /**
         ByteBuffer buf = ByteBuffer.allocate(13 + DIRECT_POINTERS_SIZE * 4);
         buf.put(serializedForm);
         buf.rewind();
         isFile = buf.get();
         size = buf.getInt();
         blocks = buf.getInt();
         for(int i = 0; i < DIRECT_POINTERS_SIZE; i++){
         data[i] = buf.getInt();
         }
         nextChunk = buf.getInt();         */
    }

    public byte[] toByteArray() {
        ByteBuffer buf = ByteBuffer.allocate(getSerializedSize());
        buf.put(isFile);
        buf.putInt(size);
        buf.putInt(blocks);
        for (int i = 0; i < DIRECT_POINTERS_SIZE; i++) {
            buf.putInt(data[i]);
        }
        buf.putInt(nextChunk);
        return buf.array();
    }

    @Override
    public String toString() {
        return "INode2{" +
                "isFile=" + isFile +
                ", size=" + size +
                ", blocks=" + blocks +
                ", data=" + Arrays.toString(data) +
                ", nextChunk=" + nextChunk +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        INode node2 = (INode) o;

        if (blocks != node2.blocks) return false;
        if (isFile != node2.isFile) return false;
        if (nextChunk != node2.nextChunk) return false;
        if (size != node2.size) return false;
        if (!Arrays.equals(data, node2.data)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) isFile;
        result = 31 * result + size;
        result = 31 * result + blocks;
        result = 31 * result + Arrays.hashCode(data);
        result = 31 * result + nextChunk;
        return result;
    }
}
