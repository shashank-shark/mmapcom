package com.mmapcom.mmap;

import java.io.EOFException;
import java.io.IOException;
import com.mmapcom.mmap.MemoryModelConstants.*;

public class MMapWriter {

    private MMapFile memoryFile;
    private final String fileName;
    private final long fileSize;
    private final int entrySize;

    public MMapWriter(String fileName, long fileSize, int recordSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.entrySize = recordSize + MemoryModelConstants.Length.RECORD_HEADER;
    }

    public void open() throws IOException {
        try {
            memoryFile = new MMapFile(fileName, fileSize);
        } catch (Exception exception) {
            throw new IOException("Failed to open the file: " + exception.getMessage());
        }
        memoryFile.compareAndSwapLong(Structure.LIMIT, 0, Structure.DATA);
    }

    public boolean write(Message message) throws EOFException {
        long commitPosition = writeRecord(message);
        return commit(commitPosition);
    }

    public long writeRecord(Message message) throws EOFException {
        long limit = allocate();
        long commitPosition = limit;
        limit = limit + Length.STATUS_FLAG;
        memoryFile.putInt(limit, message.type());
        limit = limit + Length.METADATA;
        message.write(memoryFile, limit);
        return commitPosition;
    }

    public boolean write(byte[] src, int offset, int length) throws EOFException {
        long commitPosition = writeRecord(src, offset, length);
        return commit(commitPosition);
    }

    protected long writeRecord(byte[] src, int offset, int length) throws EOFException {
        long limit = allocate();
        long commitPosition = limit;
        limit = limit + Length.STATUS_FLAG;
        memoryFile.putInt(limit, length);
        limit = limit + Length.METADATA;
        memoryFile.setBytes(limit, src, offset, length);
        return  commitPosition;
    }

    private long allocate() throws EOFException {
        long limit = memoryFile.getAndAddLong(Structure.LIMIT, entrySize);
        if (limit + entrySize > fileSize) {
            throw new EOFException("End of file reached: ");
        }
        return limit;
    }

    protected boolean commit (long commitPosition) {
        return memoryFile.compareAndSwapInt(commitPosition, StatusFlag.NOT_SET, StatusFlag.COMMIT);
    }

    public void close() throws IOException {
        try {
            memoryFile.unmap();
        } catch (Exception exception) {
            throw new IOException("Unable to dereference the memory mapped file: " + exception.getMessage());
        }
    }

}
