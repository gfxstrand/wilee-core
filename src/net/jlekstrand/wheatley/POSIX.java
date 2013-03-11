package net.jlekstrand.wheatley;

import java.io.IOException;

public class POSIX
{
    public native static void pipe(int pipefd[]) throws IOException;
    public native static void close(int fd) throws IOException;
    public native static void read(int fd, byte buf[], long count)
            throws IOException;
    public static void read(int fd, byte buf[]) throws IOException
    {
        read(fd, buf, buf.length);
    }
    public native static void write(int fd, byte buf[], long count)
            throws IOException;
    public static void write(int fd, byte buf[]) throws IOException
    {
        write(fd, buf, buf.length);
    }
    private native static void _setenv(byte name[], byte value[],
            boolean overwrite);
    public static void setenv(String name, String value, boolean overwrite)
    {
        _setenv(name.getBytes(), value.getBytes(), overwrite);
    }

    static {
        System.loadLibrary("wheatley-lib");
    }
}

