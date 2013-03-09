package net.jlekstrand.wheatley;

import java.nio.ByteBuffer;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_shm_pool;

public class ShmPool extends Resource implements wl_shm_pool.Requests
{
    private int refCount;
    private org.freedesktop.wayland.ShmPool pool;

    public ShmPool(int id, int fd, int size)
    {
        super(wl_shm_pool.WAYLAND_INTERFACE, id);
        this.refCount = 1;
        try {
            this.pool = org.freedesktop.wayland.ShmPool.fromFileDescriptor(
                    fd, size, false, true);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ByteBuffer getBuffer()
    {
        return pool.asByteBuffer();
    }

    @Override
	public void createBuffer(Client client, int id, int offset, int width,
            int height, int stride, int format)
    {
        if (width < 0 || height < 0 || stride < 0 || offset < 0)
            throw new ArrayIndexOutOfBoundsException();

        if (stride < width * 4 || height * stride + offset > pool.size())
            throw new ArrayIndexOutOfBoundsException();

        // Yeah, there's no error checking yet... That needs to be fixed
        ShmBuffer buffer = new ShmBuffer(id, this, offset, width, height,
                stride, format);
        client.addResource(buffer);

        ++refCount;
    }

    public void release()
    {
        --refCount;

        if (refCount == 0 && pool != null) {
            try {
                pool.close();
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
            pool = null;
        }
    }

    @Override
    public void destroy(Client client)
    {
        release();
        super.destroy();
    }

    @Override
	public void resize(Client client, int size)
    {
        try {
            pool.resize(size);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}

