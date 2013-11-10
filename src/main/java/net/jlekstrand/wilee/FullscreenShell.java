/*
 * Copyright © 2013 Jason Ekstrand.
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
import net.jlekstrand.wilee.protocol.wl_fullscreen_shell;

import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_surface;

class FullscreenShell extends Global implements Shell, wl_fullscreen_shell.Requests
{
    private static final String LOG_PREFIX = "FullscreenShell";

    private class SurfaceIterator implements ListIterator<Surface>
    {
        private int pos;

        public SurfaceIterator()
        {
            pos = 0;
        }

        @Override
        public void add(Surface s)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext()
        {
            return pos == 0 && presentedSurface != null;
        }

        @Override
        public boolean hasPrevious()
        {
            return pos == 1;
        }

        @Override
        public Surface next()
        {
            if (pos > 0 || presentedSurface == null)
                throw new NoSuchElementException();

            ++pos;

            return presentedSurface;
        }

        @Override
        public int nextIndex()
        {
            return pos;
        }

        @Override
        public Surface previous()
        {
            if (pos <= 0 || presentedSurface == null)
                throw new NoSuchElementException();

            --pos;

            return presentedSurface;
        }

        @Override
        public int previousIndex()
        {
            return pos - 1;
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

    private Client boundClient;
    private int bindCount;

    private Surface presentedSurface;

    public FullscreenShell(Display display)
    {
        super(display, wl_fullscreen_shell.WAYLAND_INTERFACE, 1);

        bindCount = 0;
    }

    @Override
    public ListIterator<Surface> getVisibleSurfaces()
    {
        return new SurfaceIterator();
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
        if (boundClient != null && client != boundClient) {
            client.destroy();
            return;
        }

        wl_fullscreen_shell.Resource res =
                new wl_fullscreen_shell.Resource(client, 1, id);
        res.setImplementation(this);

        bindCount++;
        res.addDestroyListener(new DestroyListener() {
            @Override
            public void onDestroy()
            {
                bindCount--;
                if (bindCount <= 0)
                    boundClient = null;
            }
        });
    }

    @Override
    public void presentSurface(wl_fullscreen_shell.Resource resource,
            Resource surfaceRes, int method, int framerate, Resource output)
    {
        presentedSurface = Surface.fromResource(surfaceRes);
    }
}

