package org.test.model;

/**
 * Created by nay on 1/6/2015.
 */
public class DirData {
    public DirDataEntry[] children;

    public static class DirDataEntry{
        public String name;
        public int inode;
    }
}
