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

    private long addr, size;
    private final String loc;

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

    private void mapAndSetOffset() throws Exception {
        final RandomAccessFile initiateAndReturnAddressFile = new RandomAccessFile(this.loc, "rw");
        initiateAndReturnAddressFile.setLength(this.size);
        final FileChannel fileChannel = initiateAndReturnAddressFile.getChannel();
        this.addr = (long) mmap.invoke(fileChannel, 1, 0L, this.size);
        fileChannel.close();
        initiateAndReturnAddressFile.close();
    }

    protected void unmap() throws Exception {
        unmmap.invoke(null, addr, this.size);
    }
}
