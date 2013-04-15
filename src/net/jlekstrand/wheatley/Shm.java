package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_shm;

class Shm extends Global implements wl_shm.Requests
{
    public Shm()
    {
        super(wl_shm.WAYLAND_INTERFACE);
    }

    @Override
    public void bindClient(Client client, int version, int id)
    {
        wl_shm.Resource res = new wl_shm.Resource(client, id, this);
        publishFormats(res);
    }

    private void publishFormats(wl_shm.Resource res)
    {
        res.format(wl_shm.FORMAT_ARGB8888);
        res.format(wl_shm.FORMAT_XRGB8888);
    }

    @Override
	public void createPool(wl_shm.Resource resource, int id, int fd, int size)
    {
        new ShmPool(resource.getClient(), id, fd, size);
    }
}

