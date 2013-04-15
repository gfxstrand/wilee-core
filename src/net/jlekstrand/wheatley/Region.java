package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_region;

import android.graphics.Rect;

class Region implements wl_region.Requests
{
    public final wl_region.Resource resource;
    private android.graphics.Region androidRegion;

    public Region(Client client, int id)
    {
        resource = new wl_region.Resource(client, id, this);

        androidRegion = new android.graphics.Region();
    }

    android.graphics.Region getRegion()
    {
        return androidRegion;
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
        androidRegion.op(new Rect(x, y, x + width, y + height),
                android.graphics.Region.Op.UNION);
    }

    @Override
	public void subtract(wl_region.Resource resource, int x, int y, int width,
            int height)
    {
        // Not sure if DIFFERENCE or REVERSE_DIFFERENCE is appropreate here
        androidRegion.op(new Rect(x, y, x + width, y + height),
                android.graphics.Region.Op.DIFFERENCE);
    }
}

