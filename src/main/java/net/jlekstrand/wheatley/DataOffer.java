/* Copyright © 2012-2013 Jason Ekstrand.
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

import org.freedesktop.wayland.protocol.wl_data_offer;

class DataOffer implements wl_data_offer.Requests
{
    final wl_data_offer.Resource resource;
    final DataSource source;

    public DataOffer(Client client, DataSource source)
    {
        resource = new wl_data_offer.Resource(client, this);
        this.source = source;
    }

    public void sendOffer()
    {
        for (String mime_type : source.mime_types) {
            resource.offer(mime_type);
        }
    }

    @Override
    public void accept(wl_data_offer.Resource resource, int serial,
            String mime_type)
    {
        source.resource.target(mime_type);
    }

    @Override
    public void receive(wl_data_offer.Resource resource, String mime_type,
            int fd)
    {
        source.resource.send(mime_type, fd);
    }

    @Override
    public void destroy(wl_data_offer.Resource resource)
    {
        resource.destroy();
        resource = null;
    }
}

