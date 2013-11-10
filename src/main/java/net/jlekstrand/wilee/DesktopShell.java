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
package net.jlekstrand.wilee;

import net.jlekstrand.wilee.graphics.*;

import java.util.LinkedList;
import java.util.ListIterator;

import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_shell;
import org.freedesktop.wayland.protocol.wl_surface;

class DesktopShell extends Global implements Shell, wl_shell.Requests
{
    private static class SurfaceIterator implements ListIterator<Surface>
    {
        private ListIterator<ShellSurface> iter;

        public SurfaceIterator(ListIterator<ShellSurface> iter)
        {
            this.iter = iter;
        }

        @Override
        public void add(Surface s)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext()
        {
            return iter.hasNext();
        }

        @Override
        public boolean hasPrevious()
        {
            return iter.hasPrevious();
        }

        @Override
        public Surface next()
        {
            ShellSurface s = iter.next();
            if (s != null)
                return s.surface;
            else
                return null;
        }

        @Override
        public int nextIndex()
        {
            return iter.nextIndex();
        }

        @Override
        public Surface previous()
        {
            ShellSurface s = iter.previous();
            if (s != null)
                return s.surface;
            else
                return null;
        }

        @Override
        public int previousIndex()
        {
            return iter.previousIndex();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Surface s)
        {
            throw new UnsupportedOperationException();
        }
    }

    private static final String LOG_PREFIX = "DesktopShell";

    private LinkedList<ShellSurface> surfaces;

    public DesktopShell(Display display)
    {
        super(display, wl_shell.WAYLAND_INTERFACE, 1);

        surfaces = new LinkedList<ShellSurface>();
    }

    @Override
    public ListIterator<Surface> getVisibleSurfaces()
    {
        return new SurfaceIterator(surfaces.listIterator());
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
        wl_shell.Resource res = new wl_shell.Resource(client, 1, id);
        res.setImplementation(this);
    }

    @Override
    public void getShellSurface(wl_shell.Resource resource, int id,
            Resource surfaceRes)
    {
        final ShellSurface ssurface = new ShellSurface(resource.getClient(),
                id, ((ClientSurface)surfaceRes.getImplementation()).surface);

        ssurface.surface.setTransform(Matrix3.translate(-100, 0));

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

