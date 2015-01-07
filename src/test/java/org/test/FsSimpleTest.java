package org.test;

import org.junit.*;
import org.test.exception.PathDoesNotExistException;
import org.test.exception.StorageException;
import org.test.impl.FilesystemImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
//        fsFile = new File(testFileDir, testFileName);
        fsFile = File.createTempFile(testFileName, null);
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
        fs.writeFile(new String[]{"file"}, new byte[]{1, 2, 3});
    }


    @Test
    public void testReadFileContent() throws Exception {
        fs.format(fsFile, 1 << 12, 1 << 10);
        fs.createEmptyFile(new String[]{"file"});
        byte[] data = {1, 2, 3};
        fs.writeFile(new String[]{"file"}, data);
        byte[] newData = fs.readFile(new String[]{"file"});
        Assert.assertTrue(Arrays.equals(data, newData));
    }

    @Test
    public void testReadFileContentStream() throws Exception {
        fs.format(fsFile, 1 << 12, 1 << 10);
        fs.createEmptyFile(new String[]{"file"});
        byte[] data = {1, 2, 3};

        ByteArrayInputStream is = new ByteArrayInputStream(data);
        fs.writeFile(new String[]{"file"}, is);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        fs.readFile(new String[]{"file"}, os);

        Assert.assertTrue(Arrays.equals(data, os.toByteArray()));
    }


    @Test
    public void testReadBigFileContent() throws Exception {
        fs.format(fsFile, 1 << 16, 1 << 10);
        fs.createEmptyFile(new String[]{"file"});

        byte[] data = new byte[1320];
        new Random().nextBytes(data);

        fs.writeFile(new String[]{"file"}, data);
        byte[] newData = fs.readFile(new String[]{"file"});

        Assert.assertTrue(Arrays.equals(data, newData));
    }

    @Test
    public void testMaxFiles() throws Exception {
        fs.format(fsFile, 1 << 16, 1 << 2);
        //can create only 3 files (in total FS has 4 iNodes)
        fs.createEmptyFile(new String[]{"file1"});
        fs.createEmptyFile(new String[]{"file2"});
        fs.createEmptyFile(new String[]{"file3"});
    }


    @Test
    public void testRepeatedDeletion() throws Exception {
        fs.format(fsFile, 1 << 16, 1 << 2);
        //can create only 3 files (in total FS has 4 iNodes)
        fs.createEmptyFile(new String[]{"file1"});
        fs.createEmptyFile(new String[]{"file2"});
        fs.createEmptyFile(new String[]{"file3"});

        fs.rm_r(new String[]{"file1"});
        fs.rm_r(new String[]{"file2"});
        fs.rm_r(new String[]{"file3"});

        fs.createEmptyFile(new String[]{"file1"});
        fs.createEmptyFile(new String[]{"file2"});
        fs.createEmptyFile(new String[]{"file3"});
    }

    @Test(expected = StorageException.class)
    public void testTooMuchFiles() throws Exception {
        fs.format(fsFile, 1 << 16, 1 << 2);
        //can create only 3 files (in total FS has 4 iNodes)
        fs.createEmptyFile(new String[]{"file1"});
        fs.createEmptyFile(new String[]{"file2"});
        fs.createEmptyFile(new String[]{"file3"});
        //will fail
        fs.createEmptyFile(new String[]{"file4"});

    }

    @Test(expected = StorageException.class)
    public void testTooMuchContent() throws Exception {
        fs.format(fsFile, 1 << 2, 1 << 2);
        fs.createEmptyFile(new String[]{"file"});

        byte[] data = new byte[1320];
        new Random().nextBytes(data);
        fs.writeFile(new String[]{"file"}, data);

    }

    @Test
    public void testMaxContent() throws Exception {
        fs.format(fsFile, 1 << 4, 1 << 2);
        fs.createEmptyFile(new String[]{"file"});
        //only 2 blocks in FS are left for file content

        byte[] data = new byte[32];
        new Random().nextBytes(data);
        fs.writeFile(new String[]{"file"}, data);

    }

    @Test
    public void testRepeatedContentDeletion() throws Exception {
        fs.format(fsFile, 1 << 4, 1 << 2);
        fs.createEmptyFile(new String[]{"file"});
        //only 2 blocks in FS are left for file content

        byte[] data = new byte[32];
        new Random().nextBytes(data);
        fs.writeFile(new String[]{"file"}, data);

        byte[] newData = fs.readFile(new String[]{"file"});
        Assert.assertTrue(Arrays.equals(data, newData));

        //delete the file
        fs.rm_r(new String[]{"file"});

        //again create file with max content
        fs.createEmptyFile(new String[]{"file"});

        data = new byte[32];
        new Random().nextBytes(data);
        fs.writeFile(new String[]{"file"}, data);
        newData = fs.readFile(new String[]{"file"});
        Assert.assertTrue(Arrays.equals(data, newData));
    }
}
