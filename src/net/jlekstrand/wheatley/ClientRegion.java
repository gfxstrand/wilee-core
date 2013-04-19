package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Client;

import org.freedesktop.wayland.protocol.wl_region;

class ClientRegion implements wl_region.Requests
{
    public final wl_region.Resource resource;
    public final Region region;

    public ClientRegion(Client client, int id, Region.Factory factory)
    {
        resource = new wl_region.Resource(client, id, this);
        region = factory.createRegion();
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
        region.add(x, y, width, height);
    }

    @Override
	public void subtract(wl_region.Resource resource, int x, int y, int width,
            int height)
    {
        region.subtract(x, y, width, height);
    }
}

