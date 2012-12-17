package net.jlekstrand.wayland;

import org.freedesktop.wayland.server.Client;

class Shm extends org.freedesktop.wayland.server.protocol.Shm
{
    public Shm()
    {
        super(0);
    }

	public void createPool(Client client, int id, int fd, int size)
    {
        ShmPool pool = new ShmPool(id, fd, size);
        client.addResource(pool);
    }
}

