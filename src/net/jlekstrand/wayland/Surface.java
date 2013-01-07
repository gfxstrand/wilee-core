package net.jlekstrand.wayland;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.server.protocol.wl_surface;
import org.freedesktop.wayland.server.protocol.wl_region;
import org.freedesktop.wayland.server.protocol.wl_buffer;

class Surface extends Resource implements wl_surface.Requests
{
    public Surface(int id)
    {
        super(wl_surface.WAYLAND_INTERFACE, id);
    }

    @Override
	public void attach(Client client, wl_buffer buffer, int x, int y)
    {
    }

    @Override
	public void damage(Client client, int x, int y, int width, int height)
    {
    }

    @Override
	public void frame(Client client, int callback)
    {
    }

    @Override
	public void setOpaqueRegion(Client client, wl_region region)
    {
    }

    @Override
	public void setInputRegion(Client client, wl_region region)
    {
    }

    @Override
	public void commit(Client client)
    {
    }

    @Override
	public void setBufferTransform(Client client, int transform)
    {
    }
}

