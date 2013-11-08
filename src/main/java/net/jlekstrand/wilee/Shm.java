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

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_shm;

class Shm extends Global implements wl_shm.Requests
{
    public Shm(Display display)
    {
        super(display, wl_shm.WAYLAND_INTERFACE, 1);
    }

    @Override
    public void bindClient(Client client, int version, int id)
    {
        wl_shm.Resource res = new wl_shm.Resource(client, 1, id);
        res.setImplementation(this);
        publishFormats(res);
    }

    private void publishFormats(wl_shm.Resource res)
    {
        res.format(wl_shm.FORMAT_ARGB8888);
        res.format(wl_shm.FORMAT_XRGB8888);
    }

    @Override
	public void createPool(wl_shm.Resource resource, int id, int fd, int size)
    {
        new ShmPool(resource.getClient(), id, fd, size);
    }
}

