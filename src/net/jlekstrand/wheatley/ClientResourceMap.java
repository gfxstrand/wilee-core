package net.jlekstrand.wheatley;

import java.util.HashMap;

import org.freedesktop.wayland.server.Listener;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.Client;

class ClientResourceMap
{
    private final HashMap<Client, Resource> resources;

    public ClientResourceMap()
    {
        resources = new HashMap<Client, Resource>();
    }

    public void addResource(Resource resource)
    {
        final Client client = resource.getClient();

        resources.put(client, resource);

        resource.addDestroyListener(new Listener () {
            public void onNotify()
            {
                removeResource(client);
            }
        });
    }

    public Resource getResource(Client client)
    {
        return resources.get(client);
    }

    public Resource removeResource(Client client)
    {
        return resources.remove(client);
    }

    public void removeResource(Resource resource)
    {
        resources.remove(resource.getClient());
    }
}

