package net.jlekstrand.wayland;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.protocol.Region;
import org.freedesktop.wayland.server.protocol.Buffer;

class Surface extends org.freedesktop.wayland.server.protocol.Surface
{
    public Surface(int id)
    {
        super(id);
    }

	public void attach(Client client, Buffer buffer, int x, int y)
    {
    }

	public void damage(Client client, int x, int y, int width, int height)
    {
    }

	public void frame(Client client, int callback)
    {
    }

	public void setOpaqueRegion(Client client, Region region)
    {
    }

	public void setInputRegion(Client client, Region region)
    {
    }

	public void commit(Client client)
    {
    }

	public void setBufferTransform(Client client, int transform)
    {
    }
}

