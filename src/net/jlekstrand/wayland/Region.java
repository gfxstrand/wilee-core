package net.jlekstrand.wayland;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.server.protocol.wl_region;

import android.graphics.Rect;

class Region extends Resource implements wl_region.Requests
{
    android.graphics.Region androidRegion;

    public Region(int id)
    {
        super(wl_region.WAYLAND_INTERFACE, id);
        androidRegion = new android.graphics.Region();
    }

    @Override
	public void add(Client client, int x, int y, int width, int height)
    {
        androidRegion.op(new Rect(x, y, x + width, y + height),
                android.graphics.Region.Op.UNION);
    }

    @Override
	public void subtract(Client client, int x, int y, int width, int height)
    {
        // Not sure if DIFFERENCE or REVERSE_DIFFERENCE is appropreate here
        androidRegion.op(new Rect(x, y, x + width, y + height),
                android.graphics.Region.Op.DIFFERENCE);
    }
}

