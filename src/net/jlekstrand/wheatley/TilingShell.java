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

import java.util.LinkedList;

import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_shell;
import org.freedesktop.wayland.protocol.wl_surface;

class TilingShell extends Global implements Shell
{
    private static final String LOG_PREFIX = "TilingShell";

    private LinkedList<ShellSurface> surfaces;

    public TilingShell()
    {
        super(wl_shell.WAYLAND_INTERFACE);

        surfaces = new LinkedList<ShellSurface>();
    }

    public void render(Renderer renderer)
    {
        renderer.beginRender(true);

        for (ShellSurface ssurface : surfaces)
            renderer.drawSurface(ssurface.surface);

        int serial = renderer.endRender();

        for (ShellSurface ssurface : surfaces)
            ssurface.surface.notifyFrameCallbacks(serial);
    }

    @Override
    public boolean surfaceDamaged(Surface surface, Region damage)
    {
        /* Always redraw */
        return true;
    }

    @Override
    public void bindClient(Client client, int version, int id)
    {
        client.addObject(wl_shell.WAYLAND_INTERFACE, id, this);
    }

    @Override
    public void getShellSurface(wl_shell.Resource resource, int id,
            Resource surfaceRes)
    {
        final ShellSurface ssurface = new ShellSurface(resource.getClient(),
                id, (Surface)surfaceRes.getData());

        surfaces.add(ssurface);
        ssurface.resource.addDestroyListener(new DestroyListener() {
            @Override
            public void onDestroy()
            {
                surfaces.remove(ssurface);
            }
        });
    }
}

