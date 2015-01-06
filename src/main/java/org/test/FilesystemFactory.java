package org.test;

import org.test.impl.FilesystemImpl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nay on 1/2/2015.
 */
public class FilesystemFactory {
    private static Map<String, Filesystem> filesystems = new HashMap<>();

    public static synchronized Filesystem getFilesystem(File storageFile) {
        String filepath = storageFile.getAbsolutePath();
        Filesystem res = filesystems.get(filepath);
//        if (res == null) {
//            res = new FilesystemImpl(storageFile);
//            filesystems.put(filepath, res);
//        }
        return res;
    }
}
