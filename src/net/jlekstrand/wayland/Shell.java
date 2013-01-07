package net.jlekstrand.wayland;

import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.Client;

import org.freedesktop.wayland.server.protocol.wl_shell;
import org.freedesktop.wayland.server.protocol.wl_surface;

class Shell implements Global.BindHandler, wl_shell.Requests
{
    public Shell()
    { }

    public Global getGlobal()
    {
        return new Global(wl_shell.WAYLAND_INTERFACE, this);
    }

    @Override
    public void bindClient(Client client, int version, int id)
    {
        client.addObject(wl_shell.WAYLAND_INTERFACE, id, this);
    }

    @Override
    public void getShellSurface(Client client, int id,
            wl_surface.Requests surfaceReq)
    {
        ShellSurface ssurface = new ShellSurface(id, (Surface)surfaceReq);
        client.addResource(ssurface);
    }
}

