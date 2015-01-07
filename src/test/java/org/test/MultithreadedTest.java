package org.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.test.impl.FilesystemImpl;

import java.io.File;
import java.nio.file.Files;

/**
 * Created by nay on 1/7/2015.
 */
public class MultithreadedTest {
    private Filesystem fs;
    File fsFile;
    String testFileName = "myFsTest.dat";

    @Before
    public void setup() throws Exception {
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
    public void testOne() throws Exception{

    }
}
