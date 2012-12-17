package net.jlekstrand.wayland;

import java.nio.ByteBuffer;

import org.freedesktop.wayland.server.Client;

class ShmBuffer extends Buffer
{
    private final int width;
    private final int height;
    private final int stride;
    private final int format;

    private ByteBuffer buffer;

	public ShmBuffer(int id, int fd, int offset, int width, int height,
            int stride, int format)
    {
        super(id);
        this.width = width;
        this.height = height;
        this.stride = stride;
        this.format = format;

        this.buffer = map(fd, offset, width * stride);
    }

    @Override
    public void destroy(Client client)
    {
        if (buffer != null) {
            unmap(buffer);
            buffer = null;
        }
    }

    private static native ByteBuffer map(int fd, int offset, int size);
    private static native void unmap(ByteBuffer buffer);

    static {
        System.loadLibrary("wayland-app");
    }
}

