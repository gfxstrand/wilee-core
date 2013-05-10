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

import net.jlekstrand.wheatley.graphics.*;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.DestroyListener;

import org.freedesktop.wayland.protocol.wl_surface;
import org.freedesktop.wayland.protocol.wl_region;
import org.freedesktop.wayland.protocol.wl_buffer;
import org.freedesktop.wayland.protocol.wl_output;

import java.util.ArrayList;

public class Surface implements wl_surface.Requests
{
    public final wl_surface.Resource resource;

    private static class State
    {
        public Buffer buffer;
        public int bufferTransform;
        public final DestroyListener bufferDestroyListener = new DestroyListener() {
            public void onDestroy()
            {
                buffer = null;
            }
        };

        public Region damage = null;
        public Region inputRegion;

        public Matrix3 transform;

        public final ArrayList<Callback> callbacks = new ArrayList<Callback>();
    }

    private static final String LOG_TAG = "Surface";

    private final int id;
    private final Compositor comp;

    // These refer to the current Surface data
    private final State current;
    private State pending;

    private Matrix3 inverseTransform;

    public Surface(Client client, int id, Compositor comp)
    {
        resource = new wl_surface.Resource(client, id, this);

        this.id = id;
        this.comp = comp;

        current = new State();
        current.damage = new Region();
        current.transform = Matrix3.identity();

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

    public int getWidth()
    {
        if (current.buffer == null)
            return 0;

        switch (current.bufferTransform) {
        case wl_output.TRANSFORM_90:
        case wl_output.TRANSFORM_270:
        case wl_output.TRANSFORM_FLIPPED_90:
        case wl_output.TRANSFORM_FLIPPED_270:
            return current.buffer.getHeight();
        default:
            return current.buffer.getWidth();
        }
    }

    public int getHeight()
    {
        if (current.buffer == null)
            return 0;

        switch (current.bufferTransform) {
        case wl_output.TRANSFORM_90:
        case wl_output.TRANSFORM_270:
        case wl_output.TRANSFORM_FLIPPED_90:
        case wl_output.TRANSFORM_FLIPPED_270:
            return current.buffer.getWidth();
        default:
            return current.buffer.getHeight();
        }
    }

    public Region getDamage()
    {
        return current.damage;
    }

    public int getBufferTransform()
    {
        return current.bufferTransform;
    }

    public Matrix3 getTransform()
    {
        return current.transform;
    }

    public Matrix3 getInverseTransform()
    {
        if (inverseTransform == null)
            inverseTransform = current.transform.inverse();

        if (inverseTransform == null)
            Log.w(LOG_TAG, "Transformation matrix is singular");

        return inverseTransform;
    }

    public void setTransform(Matrix3 transform)
    {
        current.transform = transform;
        inverseTransform = null;
    }

    public boolean isInInputRegion(Point p)
    {
        final int x = Math.round(p.getX());
        final int y = Math.round(p.getY());

        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())
            return false;

        if (Log.getLevel() <= Log.VERBOSE) {
            Log.v(LOG_TAG, "Trying point (" + x + ", " + y + ")");
            Log.v(LOG_TAG, "Surface is " + getWidth() + "x" + getHeight());
        }

        if (current.inputRegion != null)
            return current.inputRegion.contains(Math.round(p.getX()),
                    Math.round(p.getY()));

        return true;
    }

    public Point fromGlobalCoordinates(Point p)
    {
        Matrix3 invTrans = getInverseTransform();

        if (invTrans == null)
            return new Point(0, 0);

        return p.transform(invTrans);
    }

    public Point toGloablCoordinates(Point p)
    {
        return p.transform(current.transform);
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

        if (buffer != null) {
            pending.buffer = (Buffer)buffer.getData();
            resource.addDestroyListener(pending.bufferDestroyListener);
        } else {
            pending.buffer = null;
        }

        pending.transform = Matrix3.translate(x, y);
    }

    @Override
	public void damage(wl_surface.Resource resource, int x, int y, int width, int height)
    {
        Rect r = new Rect(x, y, x + width, y + height);
        if (pending.damage != null)
            pending.damage = pending.damage.add(r);
        else
            pending.damage = new Region(r);
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
        return;
    }

    @Override
	public void setInputRegion(wl_surface.Resource resource, Resource region)
    {
        if (region != null) {
            net.jlekstrand.wheatley.ClientRegion cReg =
                    (net.jlekstrand.wheatley.ClientRegion)region.getData();
            pending.inputRegion = cReg.getRegion();
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
        current.bufferTransform = pending.bufferTransform;

        if (pending.damage != null) {
            current.damage.add(pending.damage);
            comp.surfaceDamaged(this, pending.damage);
        }

        if (pending.inputRegion!= null)
            current.inputRegion = pending.inputRegion;

        if (pending.transform != null)
            setTransform(current.transform.mult(pending.transform));

        current.callbacks.addAll(pending.callbacks);

        pending.bufferDestroyListener.detach();
        pending = new State();
    }

    @Override
	public void setBufferTransform(wl_surface.Resource resource, int transform)
    {
        pending.bufferTransform = transform;
    }

    @Override
    public int hashCode()
    {
        return id;
    }
}

