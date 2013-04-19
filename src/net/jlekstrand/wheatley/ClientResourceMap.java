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

import java.util.HashMap;

import org.freedesktop.wayland.server.Listener;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.Client;

class ClientResourceMap
{
    private final HashMap<Client, Resource> resources;

    public ClientResourceMap()
    {
        resources = new HashMap<Client, Resource>();
    }

    public void addResource(Resource resource)
    {
        final Client client = resource.getClient();

        resources.put(client, resource);

        resource.addDestroyListener(new Listener () {
            public void onNotify()
            {
                removeResource(client);
            }
        });
    }

    public Resource getResource(Client client)
    {
        return resources.get(client);
    }

    public Resource removeResource(Client client)
    {
        return resources.remove(client);
    }

    public void removeResource(Resource resource)
    {
        resources.remove(resource.getClient());
    }
}

