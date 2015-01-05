package org.test.model;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by nay on 1/6/2015.
 */
public class INode2 implements Serializable{
    public static final int DIRECT_POINTERS_SIZE = 10;

    public byte isFile;
    public int size;
    public int blocks;
    public int[] data = new int[DIRECT_POINTERS_SIZE];
    public int nextChunk;

    public INode2(){

    }

    public INode2(byte[] serializedForm){
        ByteBuffer buf = ByteBuffer.wrap(serializedForm);
        isFile = buf.get();
        size = buf.getInt();
        blocks = buf.getInt();
        for(int i = 0; i < DIRECT_POINTERS_SIZE; i++){
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

    public byte[] toByteArray(){
        ByteBuffer buf = ByteBuffer.allocate(13 + DIRECT_POINTERS_SIZE * 4);
        buf.put(isFile);
        buf.putInt(size);
        buf.putInt(blocks);
        for(int i = 0; i < DIRECT_POINTERS_SIZE; i++){
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

        INode2 node2 = (INode2) o;

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
