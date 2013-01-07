package net.jlekstrand.wayland;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.server.protocol.wl_buffer;

class Buffer extends Resource implements wl_buffer.Requests
{
    protected final int width;
    protected final int height;

    public Buffer(int id, int width, int height)
    {
        super(wl_buffer.WAYLAND_INTERFACE, id);

        this.width = width;
        this.height = height;
    }

    @Override
	public void destroy(Client client)
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
}

