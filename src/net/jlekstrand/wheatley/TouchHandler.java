package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.Client;

import org.freedesktop.wayland.protocol.wl_touch;

public class TouchHandler
{
    final ClientResourceMap resources;

    public TouchHandler()
    {
        resources = new ClientResourceMap();
    }

    public void bindClient(Client client, int id)
    {
        resources.addResource(client.addObject(
                wl_touch.WAYLAND_INTERFACE, id, null));
    }
}

