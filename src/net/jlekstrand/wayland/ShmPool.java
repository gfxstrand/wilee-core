package net.jlekstrand.wayland;

import java.nio.ByteBuffer;

import org.freedesktop.wayland.server.Client;

class ShmPool extends org.freedesktop.wayland.server.protocol.ShmPool
{
    private int fd;
    private int size;

    public ShmPool(int id, int fd, int size)
    {
        super(id);
        this.fd = fd;
        this.size = size;
    }

	public void createBuffer(Client client, int id, int offset, int width,
            int height, int stride, int format)
    {
        // Yeah, there's no error checking yet... That needs to be fixed
        ShmBuffer buffer = new ShmBuffer(id, fd, offset, width, height,
                stride, format);
        client.addResource(buffer);
    }

	public void resize(Client client, int size)
    {
        // TODO: This should be implemented better
        this.size = size;
    }
}

