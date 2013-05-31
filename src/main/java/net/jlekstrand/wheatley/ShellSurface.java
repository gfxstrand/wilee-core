/*
 * Copyright © 2012-2013 Jason Ekstrand.
 *  
 * Permission to use, copy, modify, distribute, and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation, and
 * that the name of the copyright holders not be used in advertising or
 * publicity pertaining to distribution of the software without specific,
 * written prior permission.  The copyright holders make no representations
 * about the suitability of this software for any purpose.  It is provided "as
 * is" without express or implied warranty.
 * 
 * THE COPYRIGHT HOLDERS DISCLAIM ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,
 * INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO
 * EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY SPECIAL, INDIRECT OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE,
 * DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE
 * OF THIS SOFTWARE.
 */
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

