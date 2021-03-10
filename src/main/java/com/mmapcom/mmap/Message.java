package com.mmapcom.mmap;

public interface Message {

    public void write (MMapFile memFile, long pos);

    public void read (MMapFile memFile, long pos);

    public int type();
}
