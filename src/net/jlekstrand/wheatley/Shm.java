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
        Resource res = client.addObject(wl_shm.WAYLAND_INTERFACE, id, this);
        publishFormats(res);
    }

    private void publishFormats(Resource res)
    {
        wl_shm.postFormat(res, wl_shm.FORMAT_ARGB8888);
        wl_shm.postFormat(res, wl_shm.FORMAT_XRGB8888);
    }

    @Override
	public void createPool(Resource resource, int id, int fd, int size)
    {
        ShmPool pool = new ShmPool(id, fd, size);
        resource.getClient().addResource(pool);
    }
}

