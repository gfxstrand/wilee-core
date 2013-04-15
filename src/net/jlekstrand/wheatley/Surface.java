package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.Listener;

import org.freedesktop.wayland.protocol.wl_surface;
import org.freedesktop.wayland.protocol.wl_region;
import org.freedesktop.wayland.protocol.wl_buffer;

import java.util.ArrayList;

import android.graphics.Rect;
import android.graphics.Region;

public class Surface implements wl_surface.Requests
{
    public final wl_surface.Resource resource;

    private static class State
    {
        public Buffer buffer;
        public Rect area;
        public final Listener bufferDestroyListener = new Listener() {
            public void onNotify()
            {
                buffer = null;
                detach();
            }
        };
        public Region damage = new Region();

        public Region inputRegion;

        public final ArrayList<Callback> callbacks = new ArrayList<Callback>();
    }

    private final int id;
    private final Compositor comp;

    // These refer to the current Surface data
    private final State current;
    private State pending;

    // These refer to pending Surface data that will get set on "commit"
    private Rect pendingBufferRect;

    public Surface(Client client, int id, Compositor comp)
    {
        resource = new wl_surface.Resource(client, id, this);

        this.id = id;
        this.comp = comp;

        current = new State();
        pending = new State();
    }

    public void notifyFrameCallbacks(int serial)
    {
        for (Callback callback : current.callbacks) {
            callback.done(serial);
        }
        current.callbacks.clear();
    }

    public Buffer getBuffer()
    {
        return current.buffer;
    }

    public Region getDamage()
    {
        return current.damage;
    }

    @Override
	public void destroy(wl_surface.Resource resource)
    {
        resource.destroy();
    }

    @Override
	public void attach(wl_surface.Resource resource, Resource buffer, int x, int y)
    {
        if (pending.buffer != null)
            pending.bufferDestroyListener.detach();

        pending.buffer = (Buffer)buffer.getData();
        pending.buffer.resource.addDestroyListener(pending.bufferDestroyListener);

        pending.area = new Rect(x, y,
                x + pending.buffer.getWidth(), y + pending.buffer.getHeight());
    }

    @Override
	public void damage(wl_surface.Resource resource, int x, int y, int width, int height)
    {
        pending.damage.op(new Rect(x, y, width, height), Region.Op.UNION);
    }

    @Override
	public void frame(wl_surface.Resource resource, int callbackID)
    {
        Callback callback = new Callback(resource.getClient(), callbackID);
        pending.callbacks.add(callback);
    }

    @Override
	public void setOpaqueRegion(wl_surface.Resource resource, Resource region)
    {
    }

    @Override
	public void setInputRegion(wl_surface.Resource resource, Resource region)
    {
        if (region != null) {
            net.jlekstrand.wheatley.Region inReg =
                    (net.jlekstrand.wheatley.Region)region.getData();
            pending.inputRegion = inReg.getRegion();
        } else {
            pending.inputRegion = null;
        }
    }

    @Override
	public void commit(wl_surface.Resource resource)
    {
        if (pending.buffer != current.buffer) {
            if (current.buffer != null) {
                current.bufferDestroyListener.detach();
                current.buffer.decrementReferenceCount();
            }
            // FIXME: Handle the resize correctly. Right now, no translations
            // are being applied.
            current.buffer = pending.buffer;

            if (current.buffer != null) {
                current.buffer.incrementReferenceCount();
                current.buffer.resource.addDestroyListener(  
                        current.bufferDestroyListener);
            }
        }

        current.damage.op(pending.damage, Region.Op.UNION);
        comp.surfaceDamaged(this, pending.damage);
        current.inputRegion = pending.inputRegion;

        current.callbacks.addAll(pending.callbacks);

        pending = new State();

        // FIXME: This should not be needed anymore, but my simple-shm build
        // still needs it
        attach(null, current.buffer.resource, 0, 0);
    }

    @Override
	public void setBufferTransform(wl_surface.Resource resource, int transform)
    {
        return;
    }

    @Override
    public int hashCode()
    {
        return id;
    }
}

