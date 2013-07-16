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

import net.jlekstrand.wheatley.graphics.Point;

import org.freedesktop.wayland.Fixed;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.DestroyListener;

import org.freedesktop.wayland.protocol.wl_data_device;

public class DataDevice implements wl_data_device.Requests
{
    public final Seat seat;

    private static final String LOG_TAG = "DataDevice";

    private class DragNDropListener implements Seat.DragListener
    {
        final DataSource source;
        wl_data_device.Resource deviceResource;
        Surface focus;
        DataOffer offer;

        public DragNDropListener(DataSource source)
        {
            this.source = source;
            this.deviceResource = null;
            this.focus = null;
            this.offer = null;
        }

        @Override
        public void motion(int time, Point p)
        {
            if (deviceResource == null)
                return;

            deviceResource.motion(time, new Fixed(p.getX()),
                    new Fixed(p.getY()));
        }

        @Override
        public void setFocus(Surface surface, Point p)
        {
            if (focus == surface)
                return;

            if (deviceResource != null) {
                deviceResource.leave();
            }

            deviceResource = null;
            focus = null;
            offer = null;

            if (surface != null && surface.getResource() != null) {
                final Client client = surface.getResource().getClient();
                deviceResource = resources.get(client);
                if (deviceResource == null)
                    return;
                focus = surface;

                if (source != null) {
                    offer = new DataOffer(client, source);
                    deviceResource.dataOffer(offer.resource);
                    offer.sendOffer();
                }
                deviceResource.enter(seat.compositor.display.nextSerial(),
                        surface.getResource(),
                        new Fixed(p.getX()), new Fixed(p.getY()),
                        offer.resource);
            }
        }

        @Override
        public void drop()
        {
            if (deviceResource == null)
                return;

            deviceResource.drop();
        }
    }

    private final HashMap<Client, wl_data_device.Resource> resources;

    private Surface icon;
    private Client dragClient;
    private DataSource source;
    private DataOffer activeOffer;

    public DataDevice(Seat seat)
    {
        this.seat = seat;
        this.resources = new HashMap<Client, wl_data_device.Resource>();
    }

    void bindClient(final Client client, int id)
    {
        wl_data_device.Resource resource =
                new wl_data_device.Resource(client, 1, id);
        resource.setImplementation(this);
        resource.addDestroyListener(new DestroyListener() {
            @Override
            public void onDestroy()
            {
                resources.remove(client);
            }
        });
        resources.put(client, resource);
    }

    @Override
    public void startDrag(wl_data_device.Resource resource, Resource sourceRes,
            Resource originSurface, Resource icon, int serial)
    {
        DataSource source = null;
        if (sourceRes != null)
            source = (DataSource)sourceRes.getData();

        Log.d(LOG_TAG, "Starting DragNDrop");

        boolean dragStarted = seat.requestDrag(new DragNDropListener(source),
                Surface.fromResource(originSurface), serial);

        if (! dragStarted)
            Log.d(LOG_TAG, "Drag attempt failed");
    }
    
    @Override
    public void setSelection(wl_data_device.Resource resource,
            Resource source, int serial)
    {
    }
}

