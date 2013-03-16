package net.jlekstrand.wheatley;

import java.lang.Runnable;
import java.lang.Thread;
import java.io.File;

import org.freedesktop.wayland.server.EventLoop;
import org.freedesktop.wayland.server.Display;
import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_compositor;
import org.freedesktop.wayland.protocol.wl_shm;

public class Compositor extends Global implements wl_compositor.Requests
{
    private static final String LOG_PREFIX = "Compositor";

    protected Display display;
    protected Shm shm;
    protected Shell shell;
    protected Renderer renderer;

    private boolean render_pending;
    private EventLoopQueuedExecutor jobExecutor;
    private Thread compositorThread;

    public Compositor()
    {
        super(wl_compositor.WAYLAND_INTERFACE);
        renderer = null;

        display = new Display();
        display.addGlobal(this);

        try {
            jobExecutor = new EventLoopQueuedExecutor();
            jobExecutor.addToEventLoop(display.getEventLoop());
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }

        shm = new Shm();
        display.addGlobal(shm);

        TilingShell tshell = new TilingShell();
        display.addGlobal(tshell);
        shell = tshell;
    }

    public Display getDisplay()
    {
        return display;
    }

    @Override
    public void bindClient(Client client, int version, int id)
    {
        client.addObject(wl_compositor.WAYLAND_INTERFACE, id, this);
    }

    public void run()
    {
        compositorThread = Thread.currentThread();
        display.run();
    }

    public void queueEvent(final Runnable runnable)
    {
        if (Thread.currentThread() == compositorThread) {
            display.getEventLoop().addIdle(new EventLoop.IdleHandler() {
                public void handleIdle()
                {
                    runnable.run();
                }
            });
        } else {
            jobExecutor.execute(runnable);
        }
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
    public void createSurface(Resource resource, int id)
    {
        Surface surface = new Surface(id, this);
        resource.getClient().addResource(surface);
    }

    @Override
    public void createRegion(Resource resource, int id)
    {
        Region region = new Region(id);
        resource.getClient().addResource(region);
    }
}

