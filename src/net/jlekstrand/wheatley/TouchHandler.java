package net.jlekstrand.wheatley;

import java.util.HashMap;

import org.freedesktop.wayland.Fixed;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.Client;

import org.freedesktop.wayland.protocol.wl_touch;

public class TouchHandler
{
    private class Finger
    {
        public final int id;

        private Surface surface;
        private Resource resource;

        public Finger(int id)
        {
            this.id = id;
        }

        public void handleDown(int serial, int time, Fixed x, Fixed y)
        {
            // surface = shell.getSurfaceAt(x.asInt(), y.asInt());
            // resource = touchResources.get(surface.resource.getClient());

            wl_touch.postDown(resource, serial, time,
                    surface.resource, id, x, y);
        }

        public void handleMotion(int time, Fixed x, Fixed y)
        {
            wl_touch.postMotion(resource, time, id, x, y);
        }

        public void handleUp(int serial, int time)
        {
            wl_touch.postUp(resource, serial, time, id);
        }
    }

    final ClientResourceMap resources;
    private HashMap<Integer, Finger> fingers;

    public TouchHandler()
    {
        resources = new ClientResourceMap();
    }

    public void bindClient(Client client, int id)
    {
        resources.addResource(client.addObject(
                wl_touch.WAYLAND_INTERFACE, id, null));
    }

    void handleDown(int serial, int time, int id, Fixed x, Fixed y)
    {
        fingers.get(id).handleDown(serial, time, x, y);
    }

    void handleMotion(int time, int id, Fixed x, Fixed y)
    {
        fingers.get(id).handleMotion(time, x, y);
    }

    void handleUp(int serial, int time, int id)
    {
        fingers.get(id).handleUp(serial, time);
    }

    void handleFrame()
    {
    }

    void handleCancel()
    {
    }
}

