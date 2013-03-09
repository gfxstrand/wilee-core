package net.jlekstrand.wheatley;

import java.io.IOException;

class POSIX
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

    static {
        System.loadLibrary("wheatley-lib");
    }
}

