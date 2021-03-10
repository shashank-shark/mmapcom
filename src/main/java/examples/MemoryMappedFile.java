package examples;

import sun.misc.Unsafe;
import sun.nio.ch.FileChannelImpl;

import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;

public class MemoryMappedFile {

    private static final String fileChannelImplMapperMethod = "map0";
    private static final String fileChannelImplUnMapperMethod = "unmap0";
    private static final String unsafeDeclareFieldName = "theUnsafe";

    private static final Unsafe unsafe;
    private static final Method mmap;
    private static final Method unmmap;
    private static final int BYTE_ARRAY_OFFSET;

    private long addr;
    private final long size;
    private final String loc;

    // static block for preforming pre-mapping operations.
    static {
        try {
            Field singletonInstanceField = Unsafe.class.getDeclaredField(unsafeDeclareFieldName);
            singletonInstanceField.setAccessible(true);
            unsafe = (Unsafe) singletonInstanceField.get(null);
            mmap = ReflectionUtils.getMethod(FileChannelImpl.class, fileChannelImplMapperMethod, int.class, long.class, long.class);
            unmmap = ReflectionUtils.getMethod(FileChannelImpl.class, fileChannelImplUnMapperMethod, long.class, long.class);
            BYTE_ARRAY_OFFSET = unsafe.arrayBaseOffset(byte[].class);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Constructor limited to this class.
     * @param loc memory mapped file location
     * @param len total size of file that needs to be mapped.
     * @throws Exception exception thrown
     */
    protected MemoryMappedFile(final String loc, long len) throws Exception {
        this.loc = loc;
        this.size = roundTo4096(len);
        mapAndSetOffset();
    }

    /**
     * This method rounds off the values in multiples of 4096.
     * Say i = 4095, then return value would be 4096.
     * @param i takes long as input (size).
     * @return rounded long value
     */
    private static long roundTo4096(long i) {
        return (i + 0xfffL) & ~0xfffL;
    }

    /**
     * Map the file into memory and get the starting offset.
     * @throws Exception exception thrown while mapping the file.
     */
    private void mapAndSetOffset() throws Exception {
        final RandomAccessFile initiateAndReturnAddressFile = new RandomAccessFile(this.loc, "rw");
        initiateAndReturnAddressFile.setLength(this.size);
        final FileChannel fileChannel = initiateAndReturnAddressFile.getChannel();
        this.addr = (long) mmap.invoke(fileChannel, 1, 0L, this.size);
        fileChannel.close();
        initiateAndReturnAddressFile.close();
    }

    /**
     * Method to unmap the file from memory.
     * @throws Exception exception thrown while unmapping the file.
     */
    protected void unmap() throws Exception {
        unmmap.invoke(null, addr, this.size);
    }

    /**
     * To access first 8 bytes.
     * @param pos the position in the memory mapped file.
     * @return byte value read from that position
     */
    public byte getByte(long pos) {
        return unsafe.getByte(pos + addr);
    }

    /**
     * To access first 8 bytes.
     * @param pos
     * @return
     */
    protected byte getByteVolatile(long pos) {
        return unsafe.getByteVolatile(null, pos + addr);
    }

    /**
     * To access first 8 bytes.
     * @param pos
     * @return
     */
    public int getInt(long pos) {
        return unsafe.getInt(pos + addr);
    }

    /**
     * To access first 8 bytes.
     * @param pos
     * @return
     */
    protected int getIntVolatile(long pos) {
        return unsafe.getIntVolatile(null, pos + addr);
    }

    /**
     * To access first 8 bytes.
     * @param pos
     * @return
     */
    public long getLong(long pos) {
        return unsafe.getLong(pos + addr);
    }

    /**
     * To access first 8 bytes.
     * @param pos
     * @return
     */
    protected long getLongVolatile(long pos) {
        return unsafe.getLongVolatile(null, addr + pos);
    }

    /**
     * To put values to first 8 bytes
     * @param pos
     * @param val
     */
    public void putByte(long pos, byte val) {
        unsafe.putByte(pos + addr, val);
    }

    /**
     * To put values to first 8 bytes
     * @param pos
     * @param val
     */
    protected void putByteVolatile(long pos, byte val) {
        unsafe.putByteVolatile(null, pos + addr, val);
    }

    /**
     * To put values to first 8 bytes
     * @param pos
     * @param val
     */
    public void putInt(long pos, int val) {
        unsafe.putInt(pos + addr, val);
    }

    /**
     * To put values to first 8 bytes
     * @param pos
     * @param val
     */
    protected void putIntVolatile(long pos, int val) {
        unsafe.putIntVolatile(null, pos + addr, val);
    }

    /**
     * To put values to first 8 bytes
     * @param pos
     * @param val
     */
    public void putLong(long pos, long val) {
        unsafe.putLong(pos + addr, val);
    }

    /**
     * To put values to first 8 bytes
     * @param pos
     * @param val
     */
    protected void putLongVolatile(long pos, long val) {
        unsafe.putLongVolatile(null, pos + addr, val);
    }

    /**
     * To write buffer data[] into memory mapped file.
     * @param pos
     * @param data
     * @param offset
     * @param length
     */
    public void setBytes(long pos, byte[] data, int offset, int length) {
        unsafe.copyMemory(data, BYTE_ARRAY_OFFSET + offset, null, pos + addr, length);
    }

    /**
     * Get the byte[] data from memory mapped file
     * @param pos
     * @param data
     * @param offset
     * @param length
     */
    public void getBytes(long pos, byte[] data, int offset, int length) {
        unsafe.copyMemory(null, pos + addr, data, BYTE_ARRAY_OFFSET + offset, length);
    }

    /**
     * To operate on first 8 bytes of data
     * @param pos
     * @param expected
     * @param value
     * @return
     */
    protected boolean compareAndSwapInt(long pos, int expected, int value) {
        return unsafe.compareAndSwapInt(null, pos + addr, expected, value);
    }

    /**
     * To operate on first 8 bytes of data
     * @param pos
     * @param expected
     * @param value
     * @return
     */
    protected boolean compareAndSwapLong(long pos, long expected, long value) {
        return unsafe.compareAndSwapLong(null, pos + addr, expected, value);
    }

    /**
     * To operate on first 8 bytes of data.
     * @param pos
     * @param delta
     * @return
     */
    protected long getAndAddLong(long pos, long delta) {
        return unsafe.getAndAddLong(null, pos + addr, delta);
    }
}
