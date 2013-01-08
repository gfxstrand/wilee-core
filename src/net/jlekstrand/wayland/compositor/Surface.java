package net.jlekstrand.wayland.compositor;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.Listener;

import org.freedesktop.wayland.server.protocol.wl_surface;
import org.freedesktop.wayland.server.protocol.wl_region;
import org.freedesktop.wayland.server.protocol.wl_buffer;

import java.util.ArrayList;

import android.graphics.Rect;
import android.graphics.Region;

class Surface extends Resource implements wl_surface.Requests
{
    // These refer to the current Surface data
    private Buffer buffer;
    private Listener bufferDestroyListener = new Listener() {
        public void onNotify()
        {
            buffer = null;
            detach();
        }
    };
    private Region damage;

    // These refer to pending Surface data that will get set on "commit"
    private Rect pendingBufferRect;
    private Buffer pendingBuffer;
    private Listener pendingBufferDestroyListener = new Listener() {
        public void onNotify()
        {
            pendingBuffer = null;
            detach();
        }
    };
    private Region pendingDamage;

    // These fields aren't really "data" as such, they just keep the surface
    // going
    private ArrayList<Callback> frameCallbacks;

    public Surface(int id)
    {
        super(wl_surface.WAYLAND_INTERFACE, id);

        damage = new Region();
        pendingDamage = new Region();

        frameCallbacks = new ArrayList<Callback>();
    }

    public void notifyFrameCallbacks(int serial)
    {
        for (Callback callback : frameCallbacks) {
            callback.done(serial);
            callback.destroy();
        }
        frameCallbacks.clear();
    }

    @Override
	public void destroy(Client client)
    {
        super.destroy();
    }

    @Override
	public void attach(Client client, wl_buffer.Requests buffer, int x, int y)
    {
        if (pendingBuffer != null)
            pendingBufferDestroyListener.detach();

        pendingBuffer = (Buffer)buffer;
        pendingBuffer.addDestroyListener(pendingBufferDestroyListener);

        pendingBufferRect = new Rect(x, y,
                x + pendingBuffer.getWidth(), y + pendingBuffer.getHeight());
    }

    @Override
	public void damage(Client client, int x, int y, int width, int height)
    {
        pendingDamage.op(new Rect(x, y, width, height), Region.Op.UNION);
    }

    @Override
	public void frame(Client client, int callbackID)
    {
        Callback callback = new Callback(callbackID);
        client.addResource(callback);
        frameCallbacks.add(callback);
    }

    @Override
	public void setOpaqueRegion(Client client, wl_region.Requests region)
    {
    }

    @Override
	public void setInputRegion(Client client, wl_region.Requests region)
    {
    }

    @Override
	public void commit(Client client)
    {
        if (pendingBuffer != buffer) {
            if (buffer != null) {
                bufferDestroyListener.detach();
                wl_buffer.postRelease(buffer);
            }
            // FIXME: Handle the resize correctly. Right now, no translations
            // are being applied.
            buffer = pendingBuffer;

            if (buffer != null) {
                buffer.addDestroyListener(bufferDestroyListener);
            }
        }

        damage.op(pendingDamage, Region.Op.UNION);
        pendingDamage = new Region();
    }

    @Override
	public void setBufferTransform(Client client, int transform)
    {
    }
}

