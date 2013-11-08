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
package net.jlekstrand.wilee;

import net.jlekstrand.wilee.graphics.Point;
import net.jlekstrand.wilee.graphics.Matrix3;

import java.util.HashMap;
import java.util.ArrayList;

import org.freedesktop.wayland.Fixed;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.Client;

import org.freedesktop.wayland.protocol.wl_touch;

public class TouchHandler
{
    private class Finger
    {
        public final int id;

        public Surface surface;
        public wl_touch.Resource resource;

        public Finger(int id)
        {
            this.id = id;
        }

        public void handleDown(int serial, int time, Point globalPos)
        {
            surface = seat.compositor.findSurfaceAt(globalPos);
            if (surface == null || surface.getResource() == null)
                return;

            Matrix3 invTrans = surface.getInverseTransform();
            if (invTrans == null)
                return;

            resource = (wl_touch.Resource)resources.getResource(
                    surface.getResource().getClient());

            Point pos = globalPos.transform(surface.getInverseTransform());

            resource.down(serial, time, surface.getResource(), id,
                    new Fixed(pos.getX()), new Fixed(pos.getY()));
        }

        public void handleMotion(int time, Point globalPos)
        {
            if (resource == null)
                return;

            Matrix3 invTrans = surface.getInverseTransform();
            if (invTrans == null)
                return;

            Point pos = globalPos.transform(invTrans);

            resource.motion(time, id, new Fixed(pos.getX()),
                    new Fixed(pos.getY()));
        }

        public void handleUp(int serial, int time)
        {
            if (resource == null)
                return;

            resource.up(serial, time, id);
        }
    }

    final Seat seat;
    final ClientResourceMap resources;
    final private HashMap<Integer, Finger> fingers;

    public TouchHandler(Seat seat)
    {
        this.seat = seat;
        resources = new ClientResourceMap();
        fingers = new HashMap<Integer, Finger>();
    }

    public void bindClient(Client client, int id)
    {
        resources.addResource(new wl_touch.Resource(client, 1, id));
    }

    public void handleDown(int serial, int time, int id, Point pos)
    {
        final Finger finger = new Finger(id);
        fingers.put(id, finger);
        finger.handleDown(serial, time, pos);
    }

    public void handleMotion(int time, int id, Point pos)
    {
        final Finger finger = fingers.get(id);
        if (finger != null)
            finger.handleMotion(time, pos);
    }

    public void handleUp(int serial, int time, int id)
    {
        final Finger finger = fingers.get(id);
        if (finger != null)
            finger.handleUp(serial, time);
    }

    public void handleFrame()
    {
        final ArrayList<Resource> activeResources = new ArrayList<Resource>();

        for (Finger finger : fingers.values())
            if (finger.resource != null &&
                    ! activeResources.contains(finger.resource))
                activeResources.add(finger.resource);

        for (Resource resource : activeResources)
            ((wl_touch.Resource)resource).frame();
    }

    public void handleCancel()
    {
        final ArrayList<Resource> activeResources = new ArrayList<Resource>();

        for (Finger finger : fingers.values())
            if (finger.resource != null &&
                    ! activeResources.contains(finger.resource))
                activeResources.add(finger.resource);

        for (Resource resource : activeResources)
            ((wl_touch.Resource)resource).cancel();
    }
}

