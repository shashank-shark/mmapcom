package examples;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MemoryMappedFileExample {

    private static int MAX_FILE_SIZE = 1 << 30;

    public static void main(String[] args) throws IOException {

        File tempFile = new File("C:\\Users\\hw1003685\\Desktop\\simple.txt");
        if (! tempFile.exists())
            tempFile.createNewFile();

        try (RandomAccessFile file = new RandomAccessFile(tempFile, "rw")) {
            FileChannel mappedFileChannel = file.getChannel();
            MappedByteBuffer mappedBuffer = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, mappedFileChannel.size());


            byte[] byteArray = mappedBuffer.array();
            int offsetAddress = mappedBuffer.arrayOffset();
            System.out.println("Offset Memory Address : " + offsetAddress);
        }
    }
}
