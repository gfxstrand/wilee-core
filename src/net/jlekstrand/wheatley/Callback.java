package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_callback;

class Callback implements wl_callback.Requests
{
    public final wl_callback.Resource resource;

    public Callback(Client client, int id)
    {
        resource = new wl_callback.Resource(client, id, this);
    }

    public void done(int serial)
    {
        resource.done(serial);
        resource.destroy();
    }
}

