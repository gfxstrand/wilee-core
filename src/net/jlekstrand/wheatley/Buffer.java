package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_buffer;

public class Buffer extends Resource implements wl_buffer.Requests
{
    protected final int width;
    protected final int height;
    protected int refCount;

    public Buffer(int id, int width, int height)
    {
        super(wl_buffer.WAYLAND_INTERFACE, id);

        this.width = width;
        this.height = height;
        
        this.refCount = 0;
    }

    @Override
	public void destroy(Resource resource)
    {
        super.destroy();
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public void incrementReferenceCount()
    {
        ++refCount;
    }

    public void decrementReferenceCount()
    {
        --refCount;

        if (refCount == 0)
            wl_buffer.postRelease(this);
    }
}

