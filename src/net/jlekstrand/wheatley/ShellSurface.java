package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_shell_surface;
import org.freedesktop.wayland.protocol.wl_surface;
import org.freedesktop.wayland.protocol.wl_seat;
import org.freedesktop.wayland.protocol.wl_output;

class ShellSurface implements wl_shell_surface.Requests
{
    public final Resource resource;
    public final Surface surface;

    public ShellSurface(Client client, int id, Surface surface)
    {
        resource = client.addObject(
                wl_shell_surface.WAYLAND_INTERFACE, id, this);

        this.surface = surface;
    }

    @Override
    public void pong(Resource resource, int serial)
    {
    }

    @Override
    public void move(Resource resource, Resource seat, int serial)
    {
    }

    @Override
    public void resize(Resource resource, Resource seat, int serial,
            int edges)
    {
    }

    @Override
    public void setToplevel(Resource resource)
    {
    }

    @Override
    public void setTransient(Resource resource, Resource parent,
            int x, int y, int flags)
    {
    }

    @Override
    public void setFullscreen(Resource resource, int method, int framerate,
            Resource output)
    {
    }

    @Override
    public void setPopup(Resource resource, Resource seat, int serial,
            Resource parent, int x, int y, int flags)
    {
    }

    @Override
    public void setMaximized(Resource resource, Resource output)
    {
    }

    @Override
    public void setTitle(Resource resource, String title)
    {
    }

    @Override
    public void setClass(Resource resource, String class_)
    {
    }
}

