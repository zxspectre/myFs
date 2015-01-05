package org.test.impl;

import org.test.Filesystem;
import org.test.exception.OutOfFileException;
import org.test.exception.PathDoesNotExistException;
import org.test.exception.StorageException;
import org.test.model.FsMeta;
import org.test.model.INode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by nay on 1/2/2015.
 */
public class FilesystemImpl implements Filesystem{
    private File storageFile;
    private FsMeta meta;

    public FilesystemImpl(File storageFile) {
        this.storageFile = storageFile;
        meta = new FsMeta();
    }

    /**
     *
     * @param dirs path of directories
     * @return INode of last directory in the path
     * @throws PathDoesNotExistException if intermediate directory was not found or if last element was actually a file, not a dir
     */
    private INode browsePath(String[] dirs) throws PathDoesNotExistException{
        INode dir = meta.getRoot();
        for(String cd: dirs){
            dir = dir.getChilds().get(cd);
            if(dir == null){
                throw new PathDoesNotExistException("One of the subdirectories in the path " + Arrays.toString(dirs) + " was not found.");
            }
            if(dir.isFile()){
                //TODO: more informative exception msg
                throw new PathDoesNotExistException(cd + " is a file.");
            }
        }
        return dir;
    }

    @Override
    public void init() throws StorageException {
        if(storageFile.exists()){
            throw new StorageException("Mounting existing FS not supported ATM.");
        }else{
            try {
                storageFile.createNewFile();
            } catch (IOException e) {
                throw new StorageException("Could not create filestorage file", e);
            }
        }
    }

    @Override
    public boolean mkdir(String[] path) throws PathDoesNotExistException, StorageException {
        //find INode for parent of the new dir
        INode dir = browsePath(Util.getParentFromPath(path));

        //check if such dir/file name is already taken
        String dirName = path[path.length - 1];
        if(dir.getChilds().get(dirName) != null){
            //dir/file name already taken
            return false;
        }else{
            //create dir
            dir.getChilds().put(dirName, new INode(false));
            return true;
        }
    }

    @Override
    public boolean createEmptyFile(String[] path) throws PathDoesNotExistException, StorageException {
        //find INode for parent of the new dir
        INode dir = browsePath(Util.getParentFromPath(path));

        //check if such dir/file name is already taken
        String dirName = path[path.length - 1];
        if(dir.getChilds().get(dirName) != null){
            //dir/file name already taken
            return false;
        }else{
            //create dir
            dir.getChilds().put(dirName, new INode(true));
            return true;
        }
    }

    @Override
    public void rm_r(String[] path) throws PathDoesNotExistException, StorageException {

    }

    @Override
    public void appendFile(String[] path, InputStream is) throws PathDoesNotExistException, OutOfFileException, StorageException, IOException {

    }


    @Override
    public void readFile(String[] path, OutputStream os) throws PathDoesNotExistException, StorageException, IOException {

    }

    @Override
    public void defragment() throws StorageException {
        //walk through the FS tree and copy data in-order to another file
    }
}
