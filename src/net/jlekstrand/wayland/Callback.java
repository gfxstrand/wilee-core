package net.jlekstrand.wayland;

import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.server.protocol.wl_callback;

class Callback extends Resource implements wl_callback.Requests
{
    public Callback(int id)
    {
        super(wl_callback.WAYLAND_INTERFACE, id);
    }

    public void done(int serial)
    {
        wl_callback.postDone(this, serial);
    }
}

