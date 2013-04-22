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

import org.freedesktop.wayland.protocol.wl_region;

class ClientRegion implements wl_region.Requests
{
    public final wl_region.Resource resource;
    Region region;

    public ClientRegion(Client client, int id)
    {
        resource = new wl_region.Resource(client, id, this);
        region = new Region();
    }

    public Region
    getRegion()
    {
        return region;
    }

    @Override
	public void destroy(wl_region.Resource resource)
    {
        resource.destroy();
    }

    @Override
	public void add(wl_region.Resource resource, int x, int y, int width,
            int height)
    {
        region = region.add(new Rect(x, y, x + width, y + height));
    }

    @Override
	public void subtract(wl_region.Resource resource, int x, int y, int width,
            int height)
    {
        region = region.subtract(new Rect(x, y, x + width, y + height));
    }
}

