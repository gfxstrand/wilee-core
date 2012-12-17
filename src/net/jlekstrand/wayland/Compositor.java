package net.jlekstrand.wayland;

import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Client;

import java.io.File;

class Compositor extends org.freedesktop.wayland.server.protocol.Compositor
{
    Display display;

    public Compositor()
    {
        super(0);
        display = new Display();
        display.addGlobal(this);

        File simple_shm = new File("/data/local/tmp", "simple-shm");
        String[] args = {
            "simple-shm"
        };
        Client.startClient(display, simple_shm, args);
    }

    public void run()
    {
        display.run();
    }

    public void createSurface(Client client, int id)
    {
        Region region = new Region(id);
        client.addResource(region);
    }

    public void createRegion(Client client, int id)
    {
        Surface surface = new Surface(id);
        client.addResource(surface);
    }
}

