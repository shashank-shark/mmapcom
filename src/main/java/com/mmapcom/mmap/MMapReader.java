package com.mmapcom.mmap;

import com.mmapcom.mmap.MemoryModelConstants.*;

import java.io.EOFException;
import java.io.IOException;

public class MMapReader {

    protected static final long MAX_TIMEOUT_COUNT = 100;
    private final String fileName;
    private final long fileSize;
    private final int recordSize;
    private MMapFile memFile;
    private long limit = Structure.DATA;
    private long prevLimit = 0;
    private long initialLimit;
    private int maxTimeout = 2000;
    protected long timerStart;
    protected long timeoutCounter;
    private boolean typeRead;

    public MMapReader(String fileName, long fileSize, int recordSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.recordSize = recordSize;
    }

    public void open() throws IOException {
        try {
            memFile = new MMapFile(fileName, fileSize);
        } catch (Exception exception) {
            throw new IOException("Unable to open the file: " + exception.getMessage());
        }

        initialLimit = memFile.getLongVolatile(Structure.LIMIT);
    }

    public void setTimeout(int timeout) {
        this.maxTimeout = timeout;
    }

    public boolean next() throws EOFException {
        if (limit >= fileSize) {
            throw new EOFException("End of file reached.");
        }

        if (prevLimit != 0 && limit - prevLimit < Length.RECORD_HEADER + recordSize) {
            limit = prevLimit + Length.RECORD_HEADER + recordSize;
        }

        if (memFile.getLongVolatile(Structure.LIMIT) <= limit) {
            return false;
        }

        int statusFlag = memFile.getIntVolatile(limit);
        if (statusFlag == StatusFlag.ROLLBACK) {
            limit = limit + Length.RECORD_HEADER + recordSize;
            prevLimit = 0;
            timeoutCounter = 0;
            timerStart = 0;
            return false;
        }

        if (statusFlag == StatusFlag.COMMIT) {
            prevLimit = limit;
            timeoutCounter = 0;
            timerStart = 0;
            return true;
        }

        timeoutCounter ++;

        if (timeoutCounter >= MAX_TIMEOUT_COUNT) {
            if (timerStart == 0) {
                timerStart = System.currentTimeMillis();
            } else {
                if (System.currentTimeMillis() - timerStart >= maxTimeout) {

                    if (! memFile.compareAndSwapInt(limit, StatusFlag.NOT_SET, StatusFlag.ROLLBACK)) {
                        return false;
                    }

                    limit = limit + Length.RECORD_HEADER + recordSize;
                    prevLimit = 0;
                    timeoutCounter = 0;
                    timerStart = 0;
                    return false;
                }
            }
        }

        return false;
    }

    public int readType() {
        typeRead = true;
        limit += Length.STATUS_FLAG;
        int type = memFile.getInt(limit);
        limit += Length.METADATA;
        return type;
    }

    public Message readMessage(Message message) {
        if (!typeRead) {
            readType();
        }
        typeRead = false;
        message.read(memFile, limit);
        limit += recordSize;
        return message;
    }

    public int readBuffer(byte[] dst, int offset) {
        limit += Length.STATUS_FLAG;
        int length = memFile.getInt(limit);
        limit += Length.METADATA;
        memFile.getBytes(limit, dst, offset, length);
        limit += recordSize;
        return length;
    }

    public boolean hasRecovered() {
        return limit >= initialLimit;
    }

    public void close() throws IOException {
        try {
            memFile.unmap();
        } catch(Exception e) {
            throw new IOException("Unable to close the file", e);
        }
    }


}
