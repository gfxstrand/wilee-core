package net.jlekstrand.wayland;

import java.nio.ByteBuffer;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.protocol.wl_shm_pool;

class ShmBuffer extends Buffer
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
        ByteBuffer buffer = pool.getBuffer();
        if (buffer != null) {
            buffer.position(offset);
            ByteBuffer slice = buffer.slice();
            slice.limit(height * stride);
            return slice;
        } else {
            return null;
        }
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
    public void destroy(Client client)
    {
        super.destroy(client);
    }
}

