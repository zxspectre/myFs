package org.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.test.exception.PathDoesNotExistException;
import org.test.exception.StorageException;
import org.test.impl.FilesystemImpl;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * The way this test is written is not to check that FS is thread safe in a known way, but to check if there's an unknown bug related to thread safety.
 * Created by nay on 1/7/2015.
 */
public class MultithreadedTest {
    private static final int MAX_THREADS = 10;
    Filesystem fs;
    File fsFile;
    String testFileName = "myFsTest.dat";
    ExecutorService service;

    @Before
    public void setup() throws Exception {
        fsFile = File.createTempFile(testFileName, null);
        if (fsFile.exists()) {
            fsFile.delete();
        }
        fs = new FilesystemImpl();
        service = Executors.newFixedThreadPool(MAX_THREADS);
    }

    @After
    public void tearDown() throws Exception {
        System.gc();
        fs.close();
        if (fsFile.exists()) {
            Files.delete(fsFile.toPath());
        }
    }

    @Test
    public void testOneThread() throws Exception {
        fs.format(fsFile, 1 << 10, 1 << 16);

        Future<Boolean> future = service.submit((new FsOperator(fs, 1, 100, 0)));

        Assert.assertTrue(future.get());
    }

    @Test
    public void testTwoThreadsNoDelay() throws Exception {
        fs.format(fsFile, 1 << 10, 1 << 16);

        Future<Boolean> future = service.submit((new FsOperator(fs, 1, 100, 0)));
        Future<Boolean> future2 = service.submit((new FsOperator(fs, 2, 100, 0)));

        Assert.assertTrue("Some threads failed, see stderr", future.get());
        Assert.assertTrue("Some threads failed, see stderr", future2.get());
    }

    @Test
    public void testTenThreadsNoDelay() throws Exception {
        fs.format(fsFile, 1 << 10, 1 << 16);

        List<FsOperator> workers = new ArrayList<>();
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            workers.add(new FsOperator(fs, i, (i+1) * 500, 0));
        }
        for (FsOperator op : workers) {
            results.add(service.submit(op));
        }
        for (Future<Boolean> res : results) {
            Assert.assertTrue("Some threads failed, see stderr", res.get());
        }
    }

    @Test
    public void testFourThreadsWithDelay() throws Exception {
        fs.format(fsFile, 1 << 10, 1 << 16);

        List<FsOperator> workers = new ArrayList<>();
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            workers.add(new FsOperator(fs, i, (i+1) * 500, 50));
        }
        for (FsOperator op : workers) {
            results.add(service.submit(op));
        }
        for (Future<Boolean> res : results) {
            Assert.assertTrue("Some threads failed, see stderr", res.get());
        }
    }


    //perform fixed amount of isolated operations, w/o any delays
    static class FsOperator implements Callable<Boolean> {
        private static final int OPERATIONS_CNT = 1000;
        Filesystem fs;
        int id;
        int maxFilesize;
        int maxDelayMs;

        public FsOperator(Filesystem fs, int id, int maxFilesize, int maxDelayMs) {
            this.id = id;
            this.fs = fs;
            this.maxFilesize = maxFilesize;
            this.maxDelayMs = maxDelayMs;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                String filename = "file" + id;
                for (int i = 0; i < OPERATIONS_CNT; i++) {
                    System.out.println(id);

                    fs.mkdir(new String[]{""+id});
                    fs.createEmptyFile(new String[]{""+id, filename});

                    if (maxDelayMs > 0)
                        Thread.sleep(new Random().nextInt(maxDelayMs));

                    byte[] data = new byte[new Random().nextInt(maxFilesize)];
                    new Random().nextBytes(data);
                    fs.writeFile(new String[]{""+id, filename}, data);

                    byte[] newData = fs.readFile(new String[]{""+id, filename});
                    fs.readFile(new String[]{""+id, filename});
                    fs.readFile(new String[]{""+id, filename});
                    fs.readFile(new String[]{""+id, filename});
                    fs.readFile(new String[]{""+id, filename});
                    Assert.assertTrue(Arrays.equals(data, newData));

                    //delete the file
                    fs.rm_r(new String[]{""+id});
                }
                //all operations succeeded
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
