package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_callback;

class Callback implements wl_callback.Requests
{
    public final Resource resource;

    public Callback(Client client, int id)
    {
        resource = client.addObject(wl_callback.WAYLAND_INTERFACE, id, this);
    }

    public void done(int serial)
    {
        wl_callback.postDone(resource, serial);
        resource.destroy();
    }
}

