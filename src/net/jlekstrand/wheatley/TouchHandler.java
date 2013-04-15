package net.jlekstrand.wheatley;

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

        public void handleDown(int serial, int time, Fixed x, Fixed y)
        {
            surface = seat.compositor.getSurfaceAt(x.asInt(), y.asInt());
            if (surface == null)
                return;

            resource = (wl_touch.Resource)resources.getResource(
                    surface.resource.getClient());

            resource.down(serial, time, surface.resource, id, x, y);
        }

        public void handleMotion(int time, Fixed x, Fixed y)
        {
            if (resource == null)
                return;

            resource.motion(time, id, x, y);
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
        resources.addResource(new wl_touch.Resource(client, id, null));
    }

    public void handleDown(int serial, int time, int id, Fixed x, Fixed y)
    {
        final Finger finger = new Finger(id);
        fingers.put(id, finger);
        finger.handleDown(serial, time, x, y);
    }

    public void handleMotion(int time, int id, Fixed x, Fixed y)
    {
        final Finger finger = fingers.get(id);
        if (finger != null)
            finger.handleMotion(time, x, y);
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

