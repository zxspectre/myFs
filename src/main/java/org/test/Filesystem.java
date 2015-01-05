package org.test;

import org.test.exception.OutOfFileException;
import org.test.exception.PathDoesNotExistException;
import org.test.exception.StorageException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Compiles on Java7. <br>
 * Behavioral contract may not be exactly homogeneous (return -vs- exceptions) but existing FS was taken as reference in this regard. <br>
 * String[] path may be non-standard way (vs String path) but this can be easily fixed
 * and current solution was taken in order not to bother with escaping (this should not be relevant for current task). <br>
 * Created by nay on 1/1/2015.
 */
public interface Filesystem {
    /**
     * Mount filesystem for further use. Should be called before any operation is performed.
     * @throws StorageException if file storage related problems occur.
     */
    void init() throws StorageException;

    /**
     * Tries to create directory for the specified path. May be blocked by operations altering FS tree.
     *
     * @param path array of strings specifying path and dir name. The 'path[path.length-1]' is the to-be-created dir name, and other String denote path for where the dir should be created
     * @return true if the directory was created successfully, false if the name was already taken (by file or directory)
     * @throws PathDoesNotExistException if one of the parent directories does not exist for the new directory
     * @throws StorageException          if problems occur with the 'File' (our FS 'hardware')
     */
    boolean mkdir(String[] path) throws PathDoesNotExistException, StorageException;

    /**
     * Tries to create file entry for the specified path. File will not contain any data. May be blocked by operations altering FS tree.
     *
     * @param path array of strings specifying path and file name. The 'path[path.length-1]' is the to-be-created file name, and other String denote path for where the file should be created
     * @return true if the file was created successfully, false if the name was already taken (by file or directory)
     * @throws PathDoesNotExistException if one of the parent directories does not exist for the new file
     * @throws StorageException          if problems occur with the 'File' (our FS 'hardware')
     */
    boolean createEmptyFile(String[] path) throws PathDoesNotExistException, StorageException;

    /**
     * Removes (in case of directories recursively) the specified path from FS.
     *
     * @param path path to the object that is to be removed. The 'path[path.length-1]' points to the file or directory name that should be removed
     * @throws PathDoesNotExistException if path to the file/directory does not exist
     * @throws StorageException          if problems occur with the 'File' (our FS 'hardware')
     */
    void rm_r(String[] path) throws PathDoesNotExistException, StorageException;

    /**
     * Read from specified InputStream and append (existing data will be retained) its contents to the specified file.<br>
     * File overwrite can be performed by rm_r() followed by appendFile().
     *
     * @param path specifies file that will have content appended to it
     * @param is   specifies input stream that contains data which should be appended to the file
     * @throws PathDoesNotExistException if one of the parent directories does not exist for the file
     * @throws OutOfFileException        if no space left on FS to store data
     * @throws StorageException          if problems occur with the 'File' (our FS 'hardware')
     * @throws IOException               if problems occur while working with specified InputStream
     */
    void appendFile(String[] path, InputStream is) throws PathDoesNotExistException, OutOfFileException, StorageException, IOException;

    /**
     * Read from specified file and output its contents to the specified output stream.
     *
     * @param path specifies file that will have content read
     * @param os   specifies output stream where the file content should be directed to
     * @throws PathDoesNotExistException if one of the parent directories does not exist for the file
     * @throws StorageException          if problems occur with the 'File' (our FS 'hardware')
     * @throws IOException               if problems occur while working with specified OutputStream
     */
    void readFile(String[] path, OutputStream os) throws PathDoesNotExistException, StorageException, IOException;

    /**
     * Performs defragmentation on the FS.
     * @throws StorageException if some problems occur while working with FS file
     */
    void defragment() throws StorageException;

}
