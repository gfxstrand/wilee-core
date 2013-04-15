package net.jlekstrand.wheatley;

import org.freedesktop.wayland.Fixed;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.Client;

import org.freedesktop.wayland.protocol.wl_pointer;

public class Pointer implements wl_pointer.Requests
{
    final ClientResourceMap resources;

    public Pointer()
    {
        resources = new ClientResourceMap();
    }

    public void bindClient(Client client, int id)
    {
        resources.addResource(new wl_pointer.Resource(client, id, this));
    }

    @Override
    public void setCursor(wl_pointer.Resource resource, int serial,
            Resource surface, Fixed hotspot_x, Fixed hotspot_y)
    { }

    public void handleMotion(int time, Fixed x, Fixed y)
    { }
}

