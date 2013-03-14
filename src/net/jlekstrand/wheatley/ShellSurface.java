package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_shell_surface;
import org.freedesktop.wayland.protocol.wl_surface;
import org.freedesktop.wayland.protocol.wl_seat;
import org.freedesktop.wayland.protocol.wl_output;

class ShellSurface extends Resource implements wl_shell_surface.Requests
{
    public final Surface surface;

    public ShellSurface(int id, Surface surface)
    {
        super(wl_shell_surface.WAYLAND_INTERFACE, id);

        this.surface = surface;
    }

    @Override
    public void pong(Resource resource, int serial)
    {
    }

    @Override
    public void move(Resource resource, wl_seat.Requests seat, int serial)
    {
    }

    @Override
    public void resize(Resource resource, wl_seat.Requests seat, int serial,
            int edges)
    {
    }

    @Override
    public void setToplevel(Resource resource)
    {
    }

    @Override
    public void setTransient(Resource resource, wl_surface.Requests parent,
            int x, int y, int flags)
    {
    }

    @Override
    public void setFullscreen(Resource resource, int method, int framerate,
            wl_output.Requests output)
    {
    }

    @Override
    public void setPopup(Resource resource, wl_seat.Requests seat, int serial,
            wl_surface.Requests parent, int x, int y, int flags)
    {
    }

    @Override
    public void setMaximized(Resource resource, wl_output.Requests output)
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

