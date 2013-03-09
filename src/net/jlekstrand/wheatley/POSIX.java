package net.jlekstrand.wheatley;

class POSIX
{
    public native static void pipe(int pipefd[]) throws IOException;
    public native static void close(int fd) throws IOException;
    public native static void read(int fd, byte buf[], long count)
            throws IOException;
    public static void read(int fd, byte buf[])
    {
        read(fd, b, b.length);
    }
    public native static void write(int fd, byte buf[], long count)
            throws IOException;
    public static void write(int fd, byte buf[])
    {
        write(fd, b, b.length);
    }

    static {
        System.loadLibrary("wheatley");
    }
}

