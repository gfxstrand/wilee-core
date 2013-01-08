package net.jlekstrand.wayland.compositor;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.server.protocol.wl_shell_surface;
import org.freedesktop.wayland.server.protocol.wl_surface;
import org.freedesktop.wayland.server.protocol.wl_seat;
import org.freedesktop.wayland.server.protocol.wl_output;

class ShellSurface extends Resource implements wl_shell_surface.Requests
{
    Surface surface;

    public ShellSurface(int id, Surface surface)
    {
        super(wl_shell_surface.WAYLAND_INTERFACE, id);

        this.surface = surface;
    }

    @Override
    public void pong(Client client, int serial)
    {
    }

    @Override
    public void move(Client client, wl_seat.Requests seat, int serial)
    {
    }

    @Override
    public void resize(Client client, wl_seat.Requests seat, int serial,
            int edges)
    {
    }

    @Override
    public void setToplevel(Client client)
    {
    }

    @Override
    public void setTransient(Client client, wl_surface.Requests parent,
            int x, int y, int flags)
    {
    }

    @Override
    public void setFullscreen(Client client, int method, int framerate,
            wl_output.Requests output)
    {
    }

    @Override
    public void setPopup(Client client, wl_seat.Requests seat, int serial,
            wl_surface.Requests parent, int x, int y, int flags)
    {
    }

    @Override
    public void setMaximized(Client client, wl_output.Requests output)
    {
    }

    @Override
    public void setTitle(Client client, String title)
    {
    }

    @Override
    public void setClass(Client client, String class_)
    {
    }
}

