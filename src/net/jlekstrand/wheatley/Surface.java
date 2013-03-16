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

public class Surface extends Resource implements wl_surface.Requests
{
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

    public Surface(int id, Compositor comp)
    {
        super(wl_surface.WAYLAND_INTERFACE, id);
        this.id = id;
        this.comp = comp;

        current = new State();
        pending = new State();
    }

    public void notifyFrameCallbacks(int serial)
    {
        for (Callback callback : current.callbacks) {
            callback.done(serial);
            callback.destroy();
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
	public void destroy(Resource resource)
    {
        super.destroy();
    }

    @Override
	public void attach(Resource resource, Resource buffer, int x, int y)
    {
        if (pending.buffer != null)
            pending.bufferDestroyListener.detach();

        pending.buffer = (Buffer)buffer.getData();
        pending.buffer.addDestroyListener(pending.bufferDestroyListener);

        pending.area = new Rect(x, y,
                x + pending.buffer.getWidth(), y + pending.buffer.getHeight());
    }

    @Override
	public void damage(Resource resource, int x, int y, int width, int height)
    {
        pending.damage.op(new Rect(x, y, width, height), Region.Op.UNION);
    }

    @Override
	public void frame(Resource resource, int callbackID)
    {
        Callback callback = new Callback(callbackID);
        resource.getClient().addResource(callback);
        pending.callbacks.add(callback);
    }

    @Override
	public void setOpaqueRegion(Resource resource, Resource region)
    {
    }

    @Override
	public void setInputRegion(Resource resource, Resource region)
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
	public void commit(Resource resource)
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
                current.buffer.addDestroyListener(  
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
        attach(null, current.buffer, 0, 0);
    }

    @Override
	public void setBufferTransform(Resource resource, int transform)
    {
        return;
    }

    @Override
    public int hashCode()
    {
        return id;
    }
}

