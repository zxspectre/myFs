package org.test.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Total directory data size (DDE_HEADER_SIZE + name.length*2) cannot exceed INT.max_val
 * Created by nay on 1/6/2015.
 */
public class DirData {
    public static final int DDE_HEADER_SIZE = 4;
    //read all block into mem, then repeat - get header, deserialize data, get header... if header is 0 - entries left.
    public List<DirDataEntry> children = new ArrayList<>();

    public DirData() {

    }

    public DirData(byte[] serializedForm) {
        if (serializedForm.length == 0) {
            throw new RuntimeException("Programmer error. Check why serialized dir data array is empty.");
        }
        int offset = 0;
        ByteBuffer headerBuf = ByteBuffer.wrap(serializedForm, offset, DDE_HEADER_SIZE);
        offset += DDE_HEADER_SIZE;
        int nextDataSize = headerBuf.getInt();
        while (nextDataSize != 0) {
            children.add(
                    new DirDataEntry(Arrays.copyOfRange(serializedForm, offset, offset + nextDataSize))
            );
            offset += nextDataSize;
            if (offset == serializedForm.length) {
                break;
            }
            headerBuf = ByteBuffer.wrap(serializedForm, offset, DDE_HEADER_SIZE);
            offset += DDE_HEADER_SIZE;
            nextDataSize = headerBuf.getInt();
        }
    }

    /**
     //how dir data with one child "a" is stored:
     //header: 6  - data: 4 bytes iNode + 2 bytes for "a"
     //header: 0 (end of children)
     //00 00 00 06    00 08 3f d3    00 61   00 00 00 00
     *  @return serialized to byte array dir data entry
     */
    public byte[] toByteArray() {
        ByteArrayOutputStream bArr = new ByteArrayOutputStream();
        try {

            for (DirDataEntry dde : children) {
                bArr.write(dde.serializeHeader());
                bArr.write(dde.serializeData());
            }
            bArr.write(getEmptyDirData());
        } catch (IOException e) {
            throw new RuntimeException("ByteArrayOutputStream should not throw IOException for 'write' method. But just in case.");
        }
        byte[] bytes = bArr.toByteArray();
        return bytes;
    }

    public static byte[] getEmptyDirData(){
        byte[] emptyDirData = new byte[DDE_HEADER_SIZE];
        Arrays.fill(emptyDirData, (byte) 0);
        return emptyDirData;
    }

    @Override
    public String toString() {
        return "DirData{" +
                "children=" + children +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DirData dirData = (DirData) o;

        if (children != null ? !children.equals(dirData.children) : dirData.children != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return children != null ? children.hashCode() : 0;
    }

    /**
     * **********************************
     * Entry class for each directory child
     * ***********************************
     */
    public static class DirDataEntry {
        public int inode;
        public String name;

        public DirDataEntry() {

        }

        public DirDataEntry(int iNode, String name) {
            this.inode = iNode;
            this.name = name;
        }

        public DirDataEntry(byte[] serializedForm) {
            ByteBuffer buf = ByteBuffer.wrap(serializedForm);
            this.inode = buf.getInt();
            StringBuilder sb = new StringBuilder();
            while (buf.hasRemaining()) {
                sb.append(buf.getChar());
            }
            this.name = sb.toString();
        }

        private int getBytesCnt() {
            return name.length() * 2 + DDE_HEADER_SIZE;
        }

        public byte[] serializeHeader() {
            ByteBuffer buf = ByteBuffer.allocate(DDE_HEADER_SIZE);
            buf.putInt(getBytesCnt());
            return buf.array();
        }

        //serialization of string will ignore codepage errors, but should be fine for ASCII symbols
        public byte[] serializeData() {
            if (this.name == null || name.isEmpty()) {
                throw new RuntimeException("Programmer error. File/dir name should not be empty.");
            }
            ByteBuffer buf = ByteBuffer.allocate(getBytesCnt());
            buf.putInt(inode);
            for (char c : name.toCharArray()) {
                buf.putChar(c);
            }
            return buf.array();
        }

        @Override
        public String toString() {
            return "DirDataEntry{" +
                    "inode=" + inode +
                    ", name='" + name + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DirDataEntry that = (DirDataEntry) o;

            if (inode != that.inode) return false;
            if (!name.equals(that.name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = inode;
            result = 31 * result + name.hashCode();
            return result;
        }
    }

}
