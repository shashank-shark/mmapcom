package com.mmapcom.mmap;

public class MemoryModelConstants {
    public static class Structure {
        public static final int LIMIT = 0;
        public static final int DATA = Length.LIMIT;
    }
    public static class Length {
        public static final int LIMIT = 8;
        public static final int STATUS_FLAG = 4;
        public static final int METADATA = 4;
        public static final int RECORD_HEADER = STATUS_FLAG + METADATA;
    }
    public static class StatusFlag {
        public static final byte NOT_SET = 0;
        public static final byte COMMIT = 1;
        public static final byte ROLLBACK = 2;
    }
}
