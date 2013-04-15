package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_buffer;

public class Buffer implements wl_buffer.Requests
{
    public final wl_buffer.Resource resource;
    protected final int width;
    protected final int height;
    protected int refCount;

    public Buffer(Client client, int id, int width, int height)
    {
        resource = new wl_buffer.Resource(client, id, this);

        this.width = width;
        this.height = height;
        
        this.refCount = 0;
    }

    @Override
	public void destroy(wl_buffer.Resource resource)
    {
        resource.destroy();
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
            resource.release();
    }
}

