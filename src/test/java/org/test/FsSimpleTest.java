package org.test;

import org.junit.*;
import org.test.exception.PathDoesNotExistException;
import org.test.impl.FilesystemImpl;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Random;

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
//        fsFile = File.createTempFile(testFileName, null);
        if (fsFile.exists()) {
            fsFile.delete();
        }
        fs = new FilesystemImpl();
    }

    @After
    public void tearDown() throws Exception {
        fs.close();
        if (fsFile.exists()) {
            Files.delete(fsFile.toPath());
        }
    }

    @Test
    public void testCreateFs() throws Exception {
        fs.format(fsFile, 1 << 12, 1 << 10);
    }

    @Test
    public void testCreateCloseOpenFs() throws Exception {
        fs.format(fsFile, 1 << 12, 1 << 10);
        fs.close();

        fs = new FilesystemImpl();
        fs.mount(fsFile);
    }

    @Test
    public void testCreateDir() throws Exception {
        fs.format(fsFile, 1 << 12, 1 << 10);
        fs.mkdir(new String[]{"newdir"});
    }


    @Test(expected = PathDoesNotExistException.class)
    public void testNonexistentCreateDir() throws Exception {
        fs.format(fsFile, 1 << 12, 1 << 10);
        fs.mkdir(new String[]{"someparent?", "newdir"});
    }

    @Test
    public void testCreateSubDir() throws Exception {
        fs.format(fsFile, 1 << 12, 1 << 10);
        fs.mkdir(new String[]{"newdir"});
        fs.mkdir(new String[]{"newdir", "onemore"});
    }


    @Test
    public void testCreateFile() throws Exception {
        fs.format(fsFile, 1 << 12, 1 << 10);
        fs.createEmptyFile(new String[]{"newfile"});
    }

    @Test
    public void testCreateFileInDir() throws Exception {
        fs.format(fsFile, 1 << 12, 1 << 10);
        fs.mkdir(new String[]{"newdir"});
        fs.createEmptyFile(new String[]{"newdir", "file"});
    }



    @Test(expected = PathDoesNotExistException.class)
    public void testCreateFileInFile() throws Exception {
        fs.format(fsFile, 1 << 12, 1 << 10);
        fs.createEmptyFile(new String[]{"newfile"});
        fs.createEmptyFile(new String[]{"newfile", "onemore"});
    }


    @Test
    public void testWriteFileContent() throws Exception {
        fs.format(fsFile, 1 << 12, 1 << 10);
        fs.createEmptyFile(new String[]{"file"});
        fs.appendFile(new String[]{"file"}, new byte[]{1,2,3});
    }


    @Test
    public void testReadFileContent() throws Exception {
        fs.format(fsFile, 1 << 12, 1 << 10);
        fs.createEmptyFile(new String[]{"file"});
        byte[] data = {1, 2, 3};
        fs.appendFile(new String[]{"file"}, data);
        byte[] newData = fs.readFile(new String[]{"file"});
        Assert.assertTrue(Arrays.equals(data, newData));
    }


    @Test
    public void testReadBigFileContent() throws Exception {
        fs.format(fsFile, 1 << 6, 1 << 10);
        fs.createEmptyFile(new String[]{"file"});

        byte[] data = new byte[1320];
        new Random().nextBytes(data);

        fs.appendFile(new String[]{"file"}, data);
        byte[] newData = fs.readFile(new String[]{"file"});
        Assert.assertTrue(Arrays.equals(data, newData));
    }
}
