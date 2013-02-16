package net.jlekstrand.wayland.compositor;

import android.util.Log;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_shm;

class Shm implements Global.BindHandler, wl_shm.Requests
{
    public Shm()
    { }

    public Global getGlobal()
    {
        return new Global(wl_shm.WAYLAND_INTERFACE, this);
    }

    @Override
    public void bindClient(Client client, int version, int id)
    {
        Log.d("Shm", "Binding SHM object");
        Resource res = client.addObject(wl_shm.WAYLAND_INTERFACE, id, this);
        publishFormats(res);
    }

    private void publishFormats(Resource res)
    {
        Log.d("Shm", "Publishing Formats");
        wl_shm.postFormat(res, wl_shm.FORMAT_ARGB8888);
        wl_shm.postFormat(res, wl_shm.FORMAT_XRGB8888);
    }

    @Override
	public void createPool(Client client, int id, int fd, int size)
    {
        Log.d("Shm", "Creating SHM Pool");
        ShmPool pool = new ShmPool(id, fd, size);
        client.addResource(pool);
    }
}

