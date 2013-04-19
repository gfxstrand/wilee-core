/*
 * Copyright © 2012-2013 Jason Ekstrand.
 *  
 * Permission to use, copy, modify, distribute, and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation, and
 * that the name of the copyright holders not be used in advertising or
 * publicity pertaining to distribution of the software without specific,
 * written prior permission.  The copyright holders make no representations
 * about the suitability of this software for any purpose.  It is provided "as
 * is" without express or implied warranty.
 * 
 * THE COPYRIGHT HOLDERS DISCLAIM ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,
 * INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO
 * EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY SPECIAL, INDIRECT OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE,
 * DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE
 * OF THIS SOFTWARE.
 */
package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.Listener;

import org.freedesktop.wayland.protocol.wl_surface;
import org.freedesktop.wayland.protocol.wl_region;
import org.freedesktop.wayland.protocol.wl_buffer;

import java.util.ArrayList;

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
        public Region damage = null;

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
        current.damage = comp.regionFactory.createRegion();
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
        if (pending.damage == null)
            pending.damage = comp.regionFactory.createRegion();

        pending.damage.add(x, y, x + width, y + height);
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
            net.jlekstrand.wheatley.ClientRegion cReg =
                    (net.jlekstrand.wheatley.ClientRegion)region.getData();
            pending.inputRegion = cReg.region.clone();
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

        if (pending.damage != null) {
            current.damage.add(pending.damage);
            comp.surfaceDamaged(this, pending.damage);
        }

        if (pending.inputRegion!= null)
            current.inputRegion = pending.inputRegion.clone();

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

