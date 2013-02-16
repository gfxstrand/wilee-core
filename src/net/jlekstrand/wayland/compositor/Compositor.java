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
    private static final String LOG_PREFIX = "Wayland:Compositor";

    private Display display;
    private Shm shm;
    private Shell shell;

    private Renderer renderer;
    boolean render_pending;

    EventLoopQueuedExecutor jobExecutor;

    public Compositor()
    {
        renderer = null;

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

        TilingShell tshell = new TilingShell();
        display.addGlobal(tshell.getGlobal());
        shell = tshell;
    }

    @Override
    public void bindClient(Client client, int version, int id)
    {
        Log.d(LOG_PREFIX, "Binding Compositor");
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

    private void requestRender()
    {
        if (render_pending)
            return;

        render_pending = true;
        display.getEventLoop().addIdle(new EventLoop.IdleHandler() {
            @Override
            public void handleIdle()
            {
                if (renderer != null && render_pending) {
                    shell.render(renderer);
                }
                render_pending = false;
                display.flushClients();
            }
        });
    }

    public void setRenderer(Renderer renderer)
    {
        Log.d(LOG_PREFIX, "Setting Renderer: " + renderer);
        this.renderer = renderer;

        if (renderer != null) {
            queueEvent(new Runnable() {
                @Override
                public void run()
                {
                    requestRender();
                }
            });
        }
    }

    public void surfaceDamaged(Surface surface, android.graphics.Region damage)
    {
        boolean needs_redraw = shell.surfaceDamaged(surface, damage);

        if (needs_redraw)
            requestRender();
    }

    @Override
    public void createSurface(Client client, int id)
    {
        Log.d(LOG_PREFIX, "Creating Surface");
        Surface surface = new Surface(id, this);
        client.addResource(surface);
    }

    @Override
    public void createRegion(Client client, int id)
    {
        Region region = new Region(id);
        client.addResource(region);
    }
}

