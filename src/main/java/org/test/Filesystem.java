package org.test;

import org.test.exception.PathDoesNotExistException;
import org.test.exception.StorageException;

import java.io.*;

/**
 * Compiles vs Java7. <br>
 *     See readme.txt for some notes on why it was implemented this way or another. <br>
 *
 * <ul>
 * <li>Behavioral contract may not be exactly homogeneous (return -vs- exceptions) but existing FS was taken as reference in this regard. </li>
 * <li>String[] path may be non-standard way (vs String path) but this can be easily fixed
 * and current solution was taken in order not to bother with escaping (this should not be relevant for current task). </li>
 * <li>Some meta-info of FS is stored in-memory. To sync it with File, 'sync' method should be called. It is called automatically on 'close'. </li>
 * <li>Only one filesystem can be opened for one underlying file.</li>
 * <li>Concurrent model is read-write lock based. Two {@link #readFile} methods are considered as 'read' all other operations (including {@link #mount}, {@link #format} and {@link #sync})
 * are considered 'write' operations.</li>
 * <li>Operations {@link #mkdir}, {@link #createEmptyFile}, {@link #writeFile} may encounter {@link StorageException} due to out of memory. In this case some rollback may be performed.
 * For {@link #writeFile} that means that file, which content we tried to update, will be deleted.</li>
 * </ul>
 * Created by nay on 1/1/2015.
 */
public interface Filesystem extends Closeable {
    /**
     * Mount filesystem for further use. 'format' or 'mount' should be called before any operation is performed.
     *
     * @throws StorageException if file storage related problems occur.
     */
    void mount(File storageFile) throws StorageException;

    /**
     * Create new filesystem for further use. 'format' or 'mount' should be called before any operation is performed.
     *
     * @param blockSize size in bytes of data block
     * @param blockCnt  total FS size in blocks
     * @throws StorageException if file storage related problems occur.
     */
    void format(File storageFile, int blockSize, int blockCnt) throws StorageException;

    /**
     * Synchronize current FS state with underlying File.
     *
     * @throws StorageException if file storage related problems occur.
     */
    void sync() throws StorageException;

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
     * Read from specified InputStream write its contents to the specified file. Old file data will be lost.<br>
     *
     * @param path specifies file that will have content appended to it
     * @param is   specifies input stream that contains data which should be written to the file
     * @throws PathDoesNotExistException if one of the parent directories does not exist for the file
     * @throws StorageException          if problems occur with the 'File' (our FS 'hardware')
     * @throws IOException               if problems occur while working with specified InputStream
     */
    void writeFile(String[] path, InputStream is) throws PathDoesNotExistException, StorageException, IOException;


    /**
     * Write byte array to the specified file. Old file data will be lost.<br>
     *
     * @param path specifies file that will have content appended to it
     * @param data byte array with data to be written to the file
     * @throws PathDoesNotExistException if one of the parent directories does not exist for the file
     * @throws StorageException          if problems occur with the 'File' (our FS 'hardware')
     * @throws IOException               if problems occur while working with specified InputStream
     */
    void writeFile(String[] path, byte[] data) throws PathDoesNotExistException, StorageException, IOException;

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
     * Read from specified file and output its contents as a byte array
     *
     * @param path specifies file that will have content read
     * @return byte array with data from file
     * @throws PathDoesNotExistException if one of the parent directories does not exist for the file
     * @throws StorageException          if problems occur with the 'File' (our FS 'hardware')
     * @throws IOException               if problems occur while working with specified OutputStream
     */
    byte[] readFile(String[] path) throws PathDoesNotExistException, StorageException, IOException;
}
