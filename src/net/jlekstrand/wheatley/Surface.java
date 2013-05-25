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

import java.util.ArrayList;

import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.protocol.wl_surface;
import org.freedesktop.wayland.protocol.wl_output;

public class Surface
{
    private static final String LOG_TAG = "Surface";

    public interface FrameCallback
    {
        public abstract void onFrameDrawn(int timestamp);
    }

    public interface RendererData
    {
        public abstract void onSurfaceDestroyed();
    }

    private final Compositor compositor;
    private final ClientSurface clientSurface;

    private Buffer buffer;
    private int bufferTransform;
    private final DestroyListener bufferDestroyListener;
    private Region damage;
    private Region inputRegion;

    private int posX;
    private int posY;
    private Matrix3 transform;

    private Matrix3 inverseTransform;

    private final ArrayList<FrameCallback> frameCallbacks;

    private RendererData rendererData;

    public Surface(Compositor compositor)
    {
        this(compositor, null);
    }

    Surface(Compositor compositor, ClientSurface clientSurface)
    {
        this.compositor = compositor;
        this.clientSurface = clientSurface;

        this.buffer = null;
        this.bufferTransform = wl_output.TRANSFORM_NORMAL;
        this.bufferDestroyListener = new DestroyListener() {
            public void onDestroy()
            {
                buffer = null;
            }
        };
        this.damage = null;
        this.inputRegion = null;

        this.posX = 0;
        this.posY = 0;
        this.transform = Matrix3.identity();
        this.inverseTransform = null;
        
        this.frameCallbacks = new ArrayList<FrameCallback>();

        this.rendererData = null;
    }

    public static Surface fromResource(Resource resource)
    {
        return ((ClientSurface)resource.getData()).surface;
    }

    public Resource getResource()
    {
        if (clientSurface != null)
            return clientSurface.resource;
        else
            return null;
    }

    public Buffer getBuffer()
    {
        return buffer;
    }

    public void setBuffer(Buffer newBuffer, int x, int y)
    {
        posX += x;
        posY += y;

        if (buffer == newBuffer)
            return;

        if (buffer != null) {
            bufferDestroyListener.detach();
            buffer.decrementReferenceCount();
        }

        buffer = newBuffer;

        if (buffer != null) {
            buffer.incrementReferenceCount();
            buffer.resource.addDestroyListener(bufferDestroyListener);
        }
    }

    public Region getDamage()
    {
        return damage;
    }

    public void addDamage(Region newDamage)
    {
        if (damage == null) {
            damage = newDamage;
        } else {
            damage = damage.add(newDamage);
        }
        compositor.surfaceDamaged(this, newDamage);
    }

    public void resetDamage()
    {
        damage = null;
    }

    public int getWidth()
    {
        if (buffer == null)
            return 0;

        switch (bufferTransform) {
        case wl_output.TRANSFORM_90:
        case wl_output.TRANSFORM_270:
        case wl_output.TRANSFORM_FLIPPED_90:
        case wl_output.TRANSFORM_FLIPPED_270:
            return buffer.getHeight();
        default:
            return buffer.getWidth();
        }
    }

    public int getHeight()
    {
        if (buffer == null)
            return 0;

        switch (bufferTransform) {
        case wl_output.TRANSFORM_90:
        case wl_output.TRANSFORM_270:
        case wl_output.TRANSFORM_FLIPPED_90:
        case wl_output.TRANSFORM_FLIPPED_270:
            return buffer.getWidth();
        default:
            return buffer.getHeight();
        }
    }

    public int getBufferTransform()
    {
        return bufferTransform;
    }

    public void setBufferTransform(int transform)
    {
        switch (transform) {
        case wl_output.TRANSFORM_NORMAL:
        case wl_output.TRANSFORM_90:
        case wl_output.TRANSFORM_180:
        case wl_output.TRANSFORM_270:
        case wl_output.TRANSFORM_FLIPPED:
        case wl_output.TRANSFORM_FLIPPED_90:
        case wl_output.TRANSFORM_FLIPPED_180:
        case wl_output.TRANSFORM_FLIPPED_270:
            break;
        default:
            throw new IllegalArgumentException("Invalid transform");
        }

        bufferTransform = transform;
    }

    public Matrix3 getTransform()
    {
        return transform;
    }

    public Matrix3 getInverseTransform()
    {
        if (inverseTransform == null)
            inverseTransform = transform.inverse();

        if (inverseTransform == null)
            Log.w(LOG_TAG, "Transformation matrix is singular");

        return inverseTransform;
    }

    public void setTransform(Matrix3 transform)
    {
        transform = transform;
        inverseTransform = null;
    }

    public void setInputRegion(Region reg)
    {
        inputRegion = reg;
    }

    public boolean isInInputRegion(Point p)
    {
        final int x = Math.round(p.getX());
        final int y = Math.round(p.getY());

        if (Log.getLevel() <= Log.VERBOSE) {
            Log.v(LOG_TAG, "Trying point (" + x + ", " + y + ")");
            Log.v(LOG_TAG, "Surface is " + getWidth() + "x" + getHeight());
        }

        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())
            return false;

        if (inputRegion != null)
            return inputRegion.contains(Math.round(p.getX()),
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
        return p.transform(transform);
    }

    public void addFrameCallback(FrameCallback cb)
    {
        frameCallbacks.add(cb);
    }

    public void frameDrawn(int timestamp)
    {
        for (FrameCallback cb : frameCallbacks) {
            cb.onFrameDrawn(timestamp);
        }
        frameCallbacks.clear();
    }

    public void setRendererData(RendererData data)
    {
        rendererData = data;
    }

    public RendererData getRendererData()
    {
        return rendererData;
    }

    public void destroy()
    {
        if (rendererData != null)
            rendererData.onSurfaceDestroyed();
    }
}

