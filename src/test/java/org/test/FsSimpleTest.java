package org.test;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.test.exception.PathDoesNotExistException;
import org.test.impl.FilesystemImpl;
import org.test.model.INode2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

/**
 * Created by nay on 1/2/2015.
 */
public class FsSimpleTest {
    private Filesystem fs;
    File fsFile;
    String testFileName = "myFsTest.dat";
//    String testFileDir = System.getProperty("java.io.tmpdir");
    String testFileDir = "C:\\temp";

    @Before
    public void setup() throws Exception {
        fsFile = new File(testFileDir, testFileName);
        fs = new FilesystemImpl(fsFile);
    }

    @After
    public void tearDown() throws Exception {
        if(fsFile.exists()) {
//            fsFile.delete();
        }
    }

    @Test
    public void testCreateDir() throws Exception {
        fs.mkdir(new String[]{"newdir"});
    }

    @Test
    public void testCreateFile() throws Exception {
        fs.createEmptyFile(new String[]{"newfile"});
    }

    @Test
    public void testCreateFileInDir() throws Exception {
        fs.mkdir(new String[]{"newdir"});
        fs.createEmptyFile(new String[]{"newdir", "file"});
    }

    @Test
    public void testCreateSubDir() throws Exception {
        fs.mkdir(new String[]{"newdir"});
        fs.mkdir(new String[]{"newdir", "onemore"});
    }

    @Test(expected = PathDoesNotExistException.class)
    public void testNonexistentCreateDir() throws Exception {
        fs.mkdir(new String[]{"someparent?", "newdir"});
    }

    @Test(expected = PathDoesNotExistException.class)
    public void testCreateFileInFile() throws Exception {
        fs.createEmptyFile(new String[]{"newfile"});
        fs.createEmptyFile(new String[]{"newfile", "onemore"});
    }

    @Ignore
    @Test
    public void testWrite() throws Exception{
        if(fsFile.exists()) {
            fsFile.delete();
        }

        fsFile.createNewFile();

        RandomAccessFile raf = new RandomAccessFile(fsFile, "rw");
        raf.seek(10);
        raf.write(new byte[]{1,2,3});

    }

}
