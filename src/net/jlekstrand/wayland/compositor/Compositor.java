package net.jlekstrand.wayland.compositor;

import java.lang.Runnable;
import java.io.File;

import android.util.Log;

import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.Client;

import org.freedesktop.wayland.protocol.wl_compositor;
import org.freedesktop.wayland.protocol.wl_shm;

public class Compositor implements Global.BindHandler, wl_compositor.Requests
{
    Display display;
    Shm shm;
    Shell shell;

    EventLoopQueuedExecutor jobExecutor;

    public Compositor()
    {
        display = new Display();
        display.addGlobal(wl_compositor.WAYLAND_INTERFACE, this);

        try {
            jobExecutor = new EventLoopQueuedExecutor();
            jobExecutor.addToEventLoop(display.getEventLoop());
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }

        shm = new Shm();
        display.addGlobal(shm.getGlobal());

        Surface surface = new Surface(0);

        shell = new Shell();
        display.addGlobal(shell.getGlobal());
    }

    @Override
    public void bindClient(Client client, int version, int id)
    {
        client.addObject(wl_compositor.WAYLAND_INTERFACE, id, this);
    }

    public void run()
    {
        File simple_shm = new File("/data/local/tmp", "simple-shm");
        String[] args = {
            "simple-shm"
        };
        Client.startClient(display, simple_shm, args);

        display.run();
    }

    public void queueEvent(Runnable runnable)
    {
        jobExecutor.execute(runnable);
    }

    @Override
    public void createSurface(Client client, int id)
    {
        Region region = new Region(id);
        client.addResource(region);
    }

    @Override
    public void createRegion(Client client, int id)
    {
        Surface surface = new Surface(id);
        client.addResource(surface);
    }
}

