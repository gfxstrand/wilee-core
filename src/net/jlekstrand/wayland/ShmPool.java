package net.jlekstrand.wayland;

import java.nio.ByteBuffer;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.protocol.wl_shm_pool;

class ShmPool extends Resource implements wl_shm_pool.Requests
{
    private int fd;
    private int size;
    private ByteBuffer buffer;

    public ShmPool(int id, int fd, int size)
    {
        super(wl_shm_pool.WAYLAND_INTERFACE, id);
        this.fd = fd;
        this.size = size;
        this.buffer = map(fd, size);
    }

    public ByteBuffer getBuffer()
    {
        if (buffer != null) {
            return buffer.asReadOnlyBuffer();
        } else {
            return null;
        }
    }

    @Override
	public void createBuffer(Client client, int id, int offset, int width,
            int height, int stride, int format)
    {
        if (width < 0 || height < 0 || stride < 0 || offset < 0)
            throw new ArrayIndexOutOfBoundsException();

        if (stride < width * 4 || height * stride + offset > size)
            throw new ArrayIndexOutOfBoundsException();

        // Yeah, there's no error checking yet... That needs to be fixed
        ShmBuffer buffer = new ShmBuffer(id, this, offset, width, height,
                stride, format);
        client.addResource(buffer);
    }

    @Override
	public void resize(Client client, int size)
    {
        // TODO: This should be implemented better
        this.size = size;
        this.buffer = map(fd, size);
    }

    @Override
    public void destroy(Client client)
    {
        if (buffer != null) {
            unmap(buffer);
            buffer = null;
        }
        super.destroy(client);
    }

    private static native ByteBuffer map(int fd, int size);
    private static native void unmap(ByteBuffer buffer);

    static {
        System.loadLibrary("wayland-app");
    }
}

