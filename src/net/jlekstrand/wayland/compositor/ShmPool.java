package net.jlekstrand.wayland.compositor;

import java.nio.ByteBuffer;

import android.util.Log;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_shm_pool;

class ShmPool extends Resource implements wl_shm_pool.Requests
{
    private static final String LOG_PREFIX = "Wayland:ShmPool";

    private int fd;
    private int size;
    private int refCount;
    private ByteBuffer buffer;

    public ShmPool(int id, int fd, int size)
    {
        super(wl_shm_pool.WAYLAND_INTERFACE, id);
        this.fd = fd;
        this.size = size;
        this.refCount = 1;
        this.buffer = map(fd, size);
    }

    public ByteBuffer getBuffer()
    {
        return buffer.asReadOnlyBuffer();
    }

    @Override
	public void createBuffer(Client client, int id, int offset, int width,
            int height, int stride, int format)
    {
        Log.d(LOG_PREFIX, "Creating SHM Buffer");

        if (width < 0 || height < 0 || stride < 0 || offset < 0)
            throw new ArrayIndexOutOfBoundsException();

        if (stride < width * 4 || height * stride + offset > size)
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

        if (refCount == 0 && buffer == null) {
            unmap(buffer);
            buffer = null;
        }
    }

    @Override
    public void destroy(Client client)
    {
        Log.d(LOG_PREFIX, "SHM Pool Destroyed");
        release();
        super.destroy();
    }

    @Override
	public void resize(Client client, int size)
    {
        // TODO: This should be implemented better
        unmap(buffer);
        this.size = size;
        this.buffer = map(fd, size);
        if (this.buffer == null)
            throw new NullPointerException();
    }

    private static native ByteBuffer map(int fd, int size);
    private static native void unmap(ByteBuffer buffer);

    static {
        System.loadLibrary("wayland-app");
    }
}

