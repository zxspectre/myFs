package org.test.impl;

import org.test.Filesystem;
import org.test.entity.DirData;
import org.test.exception.OutOfFileException;
import org.test.exception.PathDoesNotExistException;
import org.test.exception.StorageException;
import org.test.entity.INode;

import java.io.*;
import java.util.Arrays;

/**
 * Created by nay on 1/2/2015.
 */
public class FilesystemImpl implements Filesystem {
    private File storageFile;
    RandomAccessFile f;
    private FsMeta fsMeta;

    @Override
    public void mount(File storageFile) throws StorageException {
        if (!storageFile.exists()) {
            throw new StorageException("File " + storageFile.getAbsolutePath() + " does not exist, cannot mount");
        } else {
            try {
                this.storageFile = storageFile;
                f = new RandomAccessFile(storageFile, "rws");
                fsMeta = FsMeta.valueOf(f);
                //TODO
            } catch (FileNotFoundException e) {
                throw new StorageException("Error while mounting existing FS", e);
            }
        }
    }


    @Override
    public void format(File storageFile, int blockSize, int blockCnt) throws StorageException {
        if (storageFile.exists()) {
            throw new StorageException("File " + storageFile.getAbsolutePath() + " already present. Delete it manually to confirm that you want to create new FS instead of mounting an existing one.");
        } else {
            this.storageFile = storageFile;
            fsMeta = new FsMeta(blockSize, blockCnt);
            try {
                storageFile.createNewFile();
                f = new RandomAccessFile(storageFile, "rws");
            } catch (IOException e) {
                throw new StorageException("Could not create filestorage file", e);
            }
            sync();
            writeDataBlock(DirData.getEmptyDirData(), FsMeta.ROOT_I_NODE);
        }
    }

    @Override
    public void sync() throws StorageException {
        //serialize FSMeta to file
        try {
            f.seek(0);
            f.write(fsMeta.toByteArray());
        } catch (IOException e) {
            throw new StorageException("Could not write meta info on sync", e);
        }
    }

    @Override
    public boolean mkdir(String[] path) throws PathDoesNotExistException, StorageException {
        //find parent of the new dir
        INode parentNode = browsePath(Util.getParentFromPath(path));
        DirData parentDir = readDir(parentNode);

        //check if such dir/file name is already taken
        String newDirName = path[path.length - 1];
        Integer iNodeOfNewDir = tryGetDirChildByName(parentDir, newDirName);
        if (iNodeOfNewDir == null) {
            //try reserve iNode for new dir
            int newINode = fsMeta.getFreeINode().nextClear(0);
            fsMeta.getFreeINode().set(newINode);

            //try reserve data for new dir
            int newData = fsMeta.getFreeData().nextClear(0);
            fsMeta.getFreeData().set(newData);

            //update new INode
            INode newDirINode = new INode(false, newData);
            fsMeta.getINodes()[newINode] = newDirINode;

            //update parent dir info
            parentDir.children.add(new DirData.DirDataEntry(newINode, newDirName));
            overwriteDataForINode(parentNode, parentDir.toByteArray());

            //write empty dir info to data block
            writeDataBlock(DirData.getEmptyDirData(), newData);

            return true;
        } else {
            //name is taken by file
            if (fsMeta.getINodes()[iNodeOfNewDir].isFile()) {
                throw new StorageException("The " + Arrays.toString(path) + " is already taken by file");
            }
            //dir name already taken
            return false;
        }
    }


    @Override
    public boolean createEmptyFile(String[] path) throws PathDoesNotExistException, StorageException {
        //find parent of the new file
        INode parentNode = browsePath(Util.getParentFromPath(path));
        DirData parentDir = readDir(parentNode);

        //check if such dir/file name is already taken
        String newDirName = path[path.length - 1];
        Integer iNodeOfNewDir = tryGetDirChildByName(parentDir, newDirName);
        if (iNodeOfNewDir == null) {
            //try reserve iNode for new file
            int newINode = fsMeta.getFreeINode().nextClear(0);
            fsMeta.getFreeINode().set(newINode);

            //try reserve data for new file
            int newData = fsMeta.getFreeData().nextClear(0);
            fsMeta.getFreeData().set(newData);

            //update new INode
            INode newDirINode = new INode(true, newData);
            fsMeta.getINodes()[newINode] = newDirINode;

            //update parent dir info
            parentDir.children.add(new DirData.DirDataEntry(newINode, newDirName));
            overwriteDataForINode(parentNode, parentDir.toByteArray());

            //write empty file info to data block
            writeDataBlock(new byte[0], newData);

            return true;
        } else {
            //name is taken by dir
            if (!fsMeta.getINodes()[iNodeOfNewDir].isFile()) {
                throw new StorageException("The " + Arrays.toString(path) + " is already taken by directory");
            }
            //file name already taken
            return false;
        }
    }

    @Override
    public void rm_r(String[] path) throws PathDoesNotExistException, StorageException {

    }

    @Override
    public void appendFile(String[] path, InputStream is) throws PathDoesNotExistException, OutOfFileException, StorageException, IOException {
        //get file iNode
        INode file = browsePath(path);
        if (!file.isFile()) {
            throw new PathDoesNotExistException("The " + Arrays.toString(path) + " is not a file (it's directory)");
        }


    }


    @Override
    public void appendFile(String[] path, byte[] data) throws PathDoesNotExistException, OutOfFileException, StorageException, IOException {
        //get file iNode
        INode file = browse(path);
        if (!file.isFile()) {
            throw new PathDoesNotExistException("The " + Arrays.toString(path) + " is not a file (it's directory)");
        }
        overwriteDataForINode(file, data);
    }

    @Override
    public void readFile(String[] path, OutputStream os) throws PathDoesNotExistException, StorageException, IOException {

    }


    @Override
    public byte[] readFile(String[] path) throws PathDoesNotExistException, StorageException, IOException {
        //get file iNode
        INode file = browse(path);
        if (!file.isFile()) {
            throw new PathDoesNotExistException("The " + Arrays.toString(path) + " is not a file (it's directory)");
        }
        return readDataBlocks(file);
    }

    @Override
    public void close() throws IOException {
        try {
            if (f.getChannel().isOpen()) {
                sync();
                f.close();
            } else {
                System.err.println("WARN: repeated close of FS");
            }
        } catch (StorageException e) {
            throw new IOException(e);
        }
    }

    /**
     * Assuming that dirs[] is an array of directories in our FS (last one can be a file), return DirData for the last subdirectory or file in this dirs array.
     *
     * @param dirs path
     * @return target INode
     * @throws PathDoesNotExistException if intermediate directory was not found or was a file
     */
    private INode browse(String[] dirs) throws PathDoesNotExistException, StorageException {
        INode currentNode = fsMeta.getRoot();
        try {
            for (String cd : dirs) {
                if (currentNode.isFile()) {
                    //TODO: more informative exception msg (more precise info)
                    throw new PathDoesNotExistException(cd + " is a file.");
                }
                DirData currentDir = readDir(currentNode);
                currentNode = getDirChildByName(currentDir, cd);
            }
        } catch (PathDoesNotExistException e) {
            throw new PathDoesNotExistException("One of the subdirectories in the path " + Arrays.toString(dirs) + " was not found.", e);
        }

        return currentNode;
    }

    /**
     * The same as {@link #browse(String[] dirs)} but checks that resulting iNode is a directory.
     *
     * @param dirs path
     * @return target INode
     * @throws PathDoesNotExistException if intermediate directory was not found or was a file
     */
    private INode browsePath(String[] dirs) throws PathDoesNotExistException, StorageException {
        INode res = browse(dirs);
        if (res.isFile()) {
            //TODO: more informative exception msg (more precise info)
            throw new PathDoesNotExistException("One of the subdirectories in the path " + Arrays.toString(dirs) + " was not found.");
        }
        return res;
    }

    //TODO: don't like the return type of Integer here
    private Integer tryGetDirChildByName(DirData dir, String childName) {
        Integer iNodeNum = null;
        for (DirData.DirDataEntry dde : dir.children) {
            if (dde.name.equals(childName)) {
                iNodeNum = dde.inode;
            }
        }
        return iNodeNum;
    }

    private INode getDirChildByName(DirData dir, String childName) throws PathDoesNotExistException {
        Integer iNode = tryGetDirChildByName(dir, childName);
        if (iNode == null) {
            throw new PathDoesNotExistException();
        } else {
            return fsMeta.getINodes()[iNode];
        }
    }

    private DirData readDir(INode iNode) throws StorageException {
        if (iNode.isFile()) {
            throw new RuntimeException("Programmer error. Should not try to read directory structure for file iNode.");
        }
        return new DirData(readDataBlocks(iNode));
    }

    /**
     * Read file/dir data. Dir data can be de-serialized into DirData. File data can be passed outside via OutputStream.
     *
     * @param iNode dir/file inode
     * @return file/dir data
     * @throws StorageException
     */
    private byte[] readDataBlocks(INode iNode) throws StorageException {
        //copy all data block references into one int array
        int dataBlockOffset = 0;
        int dataBlocks = iNode.blocks;
        int[] dataBlockRefs = new int[dataBlocks];
        int dataBlocksInCurrentChunk = (dataBlocks - dataBlockOffset > INode.DIRECT_POINTERS_SIZE) ? INode.DIRECT_POINTERS_SIZE : dataBlocks - dataBlockOffset;
        System.arraycopy(iNode.data, 0, dataBlockRefs, dataBlockOffset, dataBlocksInCurrentChunk);
        dataBlockOffset += INode.DIRECT_POINTERS_SIZE; //this can be smaller, but if it is, then next chunk will be 0, and offset will not be required anyways

        //read other chunks if present
        INode currentChunk = iNode;
        while (currentChunk.nextChunk != 0) {
            currentChunk = fsMeta.getINodes()[currentChunk.nextChunk];
            dataBlocksInCurrentChunk = (dataBlocks - dataBlockOffset > INode.DIRECT_POINTERS_SIZE) ? INode.DIRECT_POINTERS_SIZE : dataBlocks - dataBlockOffset;
            System.arraycopy(currentChunk.data, 0, dataBlockRefs, dataBlockOffset, dataBlocksInCurrentChunk);
            dataBlockOffset += INode.DIRECT_POINTERS_SIZE; //this can be smaller, but if it is, then next chunk will be 0, and offset will not be required anyways
        }
        assert dataBlockOffset >= dataBlocks;

        //iterate over all data block references and fill resulting byte array with data
        try {
            int dataRead = 0;
            //TODO: currently read data in memory, could be better to use streaming.
            byte[] res = new byte[iNode.size];
            byte[] block = new byte[fsMeta.getBlockSize()];
            for (int dataBlockRef : dataBlockRefs) {
                int dataInCurrentBlock = (iNode.size - dataRead > fsMeta.getBlockSize()) ? fsMeta.getBlockSize() : iNode.size - dataRead;
                f.seek(fsMeta.getSerializedSize() + dataBlockRef * fsMeta.getBlockSize());
                f.readFully(block);
                System.arraycopy(block, 0, res, dataRead, dataInCurrentBlock);
                dataRead += fsMeta.getBlockSize();
            }
            return res;
        } catch (IOException e) {
            throw new StorageException("Could not read block data", e);
        }
    }

    /**
     * Overwrite data block for specified iNode. Old data will be lost.
     * TODO: optimize bitset search - consequent lookup for free bit can be performed starting from the old position.
     *
     * @param iNode
     * @param data
     */
    private void overwriteDataForINode(INode iNode, byte[] data) throws StorageException {
        System.out.println("======Start overwrite=========");

        int[] dataBlockRefs;
        int totalBlocks;

        int blockSize = fsMeta.getBlockSize();

        iNode.size = data.length;

        //first check if we need to assign additional blocks
        if (iNode.blocks * blockSize < data.length) {

            //how much blocks we need to store our data
            totalBlocks = (data.length + blockSize - 1) / blockSize;
            dataBlockRefs = new int[totalBlocks];

            //how much new data blocks we need to reserve
            int moreBlocks = totalBlocks - iNode.blocks;

            assert moreBlocks > 0;
            //check how much blocks we can assign to current iNode
            int blocksAssignable = INode.DIRECT_POINTERS_SIZE - iNode.blocks;

            if (blocksAssignable > 0) {
                System.out.println("we can fit new blocks in current iNode");
                //we can fit new blocks in current iNode
                for (int i = 0; i < blocksAssignable; i++) {
                    if (moreBlocks > 0) {
                        //get next free data chunk
                        int nextClear = fsMeta.getFreeData().nextClear(0);
                        fsMeta.getFreeData().set(nextClear);
                        //if we are here, then iNode.blocks < INode.DIRECT_POINTERS_SIZE, we can index into iNode.blocks for data
                        iNode.data[iNode.blocks++] = nextClear;
                        //one block has been assigned
                        moreBlocks--;
                    }
                }
            }

            //copy current iNode blocks to new array in case of extension
            System.arraycopy(iNode.data, 0, dataBlockRefs, 0, iNode.blocks);

            if (moreBlocks > 0) {
                //we must also create new iNode
                INode currentINode = iNode;

                while (moreBlocks > 0) {
                    System.out.println("we must also create new iNode");
                    //get free iNode
                    int newNode = fsMeta.getFreeINode().nextClear(0);
                    fsMeta.getFreeINode().set(newNode);
                    //link to old iNode
                    currentINode.nextChunk = newNode;
                    //set up new INode
                    currentINode = new INode();
                    fsMeta.getINodes()[newNode] = currentINode;
                    //try to allocate 'moreBlocks'
                    for (int i = 0; i < INode.DIRECT_POINTERS_SIZE && i < moreBlocks; i++) {
                        //get next free data chunk
                        int nextClear = fsMeta.getFreeData().nextClear(0);
                        fsMeta.getFreeData().set(nextClear);

                        //save new data block in current iNode
                        currentINode.data[i] = nextClear;
                        //increment the starting iNode block cnt and fill dataBlockRefs
                        dataBlockRefs[iNode.blocks++] = nextClear;
                    }

                    //decrease remaining block cnt by amount of direct block pointers in iNode
                    moreBlocks -= INode.DIRECT_POINTERS_SIZE;
                }
            }

        } else {
            //in case if we don't need to allocate new data blocks
            totalBlocks = iNode.blocks;
            dataBlockRefs = iNode.data;
        }
        //iterate over 'totalBlocks' amount of references from dataBlockRefs and store 'data' chunks in-order
        for (int i = 0; i < totalBlocks; i++) {
            int remainingDataForWrite = data.length - i * blockSize;
            byte[] block4Write = new byte[blockSize];
            System.arraycopy(data, i * blockSize, block4Write, 0, (remainingDataForWrite > blockSize) ? blockSize : remainingDataForWrite);
            writeDataBlock(block4Write, dataBlockRefs[i]);
        }
    }

    /**
     * Lowest lvl operation for writing data to file. No synchronization or checks of free spaces via bit sets is performed here.
     *
     * @param data    byte data to be written to underlying file.
     * @param blockNo block number in the file
     * @throws StorageException
     */
    private void writeDataBlock(byte[] data, int blockNo) throws StorageException {
        if (data.length > fsMeta.getBlockSize()) {
            throw new RuntimeException("Programmer error. By-block write should not try to write bytes more than block size.");
        }
        if (blockNo > fsMeta.getFsSize()) {
            throw new RuntimeException("Programmer error. By block write should not be called with attempt to write data to blockNo greater than FS size. It should be detected at bitmap stage.");
        }
        byte[] blockData;
        if (data.length < fsMeta.getBlockSize()) {
            //pad bytes with trailing zeros (we don't want blocks on read). Could be better way, w/o overhead for each write operation though.
            blockData = new byte[fsMeta.getBlockSize()];
            System.arraycopy(data, 0, blockData, 0, data.length);
        } else {
            blockData = data;
        }
        try {
            f.seek(fsMeta.getSerializedSize() + blockNo * fsMeta.getBlockSize());
            f.write(blockData);
        } catch (IOException e) {
            throw new StorageException("Could not write", e);
        }
    }
}
