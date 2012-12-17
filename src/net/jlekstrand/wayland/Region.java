package net.jlekstrand.wayland;

import org.freedesktop.wayland.server.Client;

import android.graphics.Rect;

class Region extends org.freedesktop.wayland.server.protocol.Region
{
    android.graphics.Region androidRegion;

    public Region(int id)
    {
        super(id);
        androidRegion = new android.graphics.Region();
    }

	public void add(Client client, int x, int y, int width, int height)
    {
        androidRegion.op(new Rect(x, y, x + width, y + height),
                android.graphics.Region.Op.UNION);
    }

	public void subtract(Client client, int x, int y, int width, int height)
    {
        // Not sure if DIFFERENCE or REVERSE_DIFFERENCE is appropreate here
        androidRegion.op(new Rect(x, y, x + width, y + height),
                android.graphics.Region.Op.DIFFERENCE);
    }
}

