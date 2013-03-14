package net.jlekstrand.wheatley;

import java.nio.ByteBuffer;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_shm_pool;

public class ShmBuffer extends Buffer
{
    private final int offset;
    private final int stride;
    private final int format;

    private final ShmPool pool;

    public ShmBuffer(int id, ShmPool pool, int offset, int width, int height,
            int stride, int format)
    {
        super(id, width, height);

        this.offset = offset;
        this.stride = stride;
        this.format = format;
        this.pool = pool;
    }

    public ByteBuffer getBuffer()
    {
        return pool.getBuffer();
    }

    public int getStride()
    {
        return stride;
    }

    public int getFormat()
    {
        return format;
    }

    @Override
    public void destroy(Resource resource)
    {
        pool.release();
        super.destroy(resource);
    }
}

