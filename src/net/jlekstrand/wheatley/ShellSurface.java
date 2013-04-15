package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_shell_surface;
import org.freedesktop.wayland.protocol.wl_surface;
import org.freedesktop.wayland.protocol.wl_seat;
import org.freedesktop.wayland.protocol.wl_output;

class ShellSurface implements wl_shell_surface.Requests
{
    public final wl_shell_surface.Resource resource;
    public final Surface surface;

    public ShellSurface(Client client, int id, Surface surface)
    {
        resource = new wl_shell_surface.Resource(client, id, this);

        this.surface = surface;
    }

    @Override
    public void pong(wl_shell_surface.Resource resource, int serial)
    {
    }

    @Override
    public void move(wl_shell_surface.Resource resource, Resource seat,
            int serial)
    {
    }

    @Override
    public void resize(wl_shell_surface.Resource resource, Resource seat,
            int serial, int edges)
    {
    }

    @Override
    public void setToplevel(wl_shell_surface.Resource resource)
    {
    }

    @Override
    public void setTransient(wl_shell_surface.Resource resource,
            Resource parent, int x, int y, int flags)
    {
    }

    @Override
    public void setFullscreen(wl_shell_surface.Resource resource, int method,
            int framerate, Resource output)
    {
    }

    @Override
    public void setPopup(wl_shell_surface.Resource resource, Resource seat,
            int serial, Resource parent, int x, int y, int flags)
    {
    }

    @Override
    public void setMaximized(wl_shell_surface.Resource resource,
            Resource output)
    {
    }

    @Override
    public void setTitle(wl_shell_surface.Resource resource, String title)
    {
    }

    @Override
    public void setClass(wl_shell_surface.Resource resource, String class_)
    {
    }
}

