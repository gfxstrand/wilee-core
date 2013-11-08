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
package net.jlekstrand.wilee;

import net.jlekstrand.wilee.graphics.*;

import java.util.ArrayList;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.DestroyListener;

import org.freedesktop.wayland.protocol.wl_surface;
import org.freedesktop.wayland.protocol.wl_callback;

public class ClientSurface implements wl_surface.Requests2
{
    final wl_surface.Resource resource;
    final Surface surface;

    private Buffer pendingBuffer;
    private boolean hasPendingBuffer;
    private int pendingBufferPosX;
    private int pendingBufferPosY;
    private DestroyListener pendingBufferDestroyListener;
    private int pendingBufferTransform;
    private Region pendingDamage;

    private Region pendingInputRegion;
    private boolean hasPendingInputRegion;

    private class Callback implements wl_callback.Requests,
            Surface.FrameCallback
    {
        public final wl_callback.Resource resource;
    
        public Callback(Client client, int id)
        {
            resource = new wl_callback.Resource(client, 1, id);
            resource.setImplementation(this);
        }
    
        @Override
        public void onFrameDrawn(int timestamp)
        {
            resource.done(timestamp);
            resource.destroy();
        }
    }

    private final ArrayList<Callback> pendingCallbacks;

    public ClientSurface(Client client, int id, Compositor compositor)
    {
        this.resource = new wl_surface.Resource(client, 1, id);
        this.resource.setImplementation(this);
        this.surface = new Surface(compositor, this);

        this.pendingBuffer = null;
        this.hasPendingBuffer = false;
        this.pendingBufferPosX = 0;
        this.pendingBufferPosY = 0;
        this.pendingBufferDestroyListener = new DestroyListener() {
            @Override
            public void onDestroy()
            {
                pendingBuffer = null;
            }
        };
        this.pendingDamage = null;

        this.pendingInputRegion = null;
        this.hasPendingInputRegion = false;

        this.pendingCallbacks = new ArrayList<Callback>();
    }

    @Override
	public void destroy(wl_surface.Resource resource)
    {
        resource.destroy();
    }

    @Override
	public void attach(wl_surface.Resource resource, Resource buffer,
            int x, int y)
    {
        if (pendingBuffer != null)
            pendingBufferDestroyListener.detach();

        if (buffer != null) {
            pendingBuffer = (Buffer)buffer.getData();
            buffer.addDestroyListener(pendingBufferDestroyListener);
        } else {
            pendingBuffer = null;
        }

        pendingBufferPosX = x;
        pendingBufferPosY = y;

        hasPendingBuffer = true;
    }

    @Override
	public void damage(wl_surface.Resource resource, int x, int y,
            int width, int height)
    {
        Rect r = new Rect(x, y, x + width, y + height);
        if (pendingDamage != null)
            pendingDamage = pendingDamage.add(r);
        else
            pendingDamage = new Region(r);
    }

    @Override
	public void frame(wl_surface.Resource resource, int callbackID)
    {
        pendingCallbacks.add(new Callback(resource.getClient(), callbackID));
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
            net.jlekstrand.wilee.ClientRegion cReg =
                    (net.jlekstrand.wilee.ClientRegion)region.getData();
            pendingInputRegion = cReg.getRegion();
        } else {
            pendingInputRegion = null;
        }

        hasPendingInputRegion = true;
    }

    @Override
	public void commit(wl_surface.Resource resource)
    {
        if (hasPendingBuffer) {
            surface.setBuffer(pendingBuffer, pendingBufferPosX,
                    pendingBufferPosY);
            pendingBufferDestroyListener.detach();
            pendingBuffer = null;
            hasPendingBuffer = false;
        }
        surface.setBufferTransform(pendingBufferTransform);

        if (pendingDamage != null) {
            surface.addDamage(pendingDamage);
            pendingDamage = null;
        }

        if (hasPendingInputRegion) {
            surface.setInputRegion(pendingInputRegion);
            pendingInputRegion = null;
            hasPendingInputRegion = false;
        }

        for (Callback cb : pendingCallbacks)
            surface.addFrameCallback(cb);
        pendingCallbacks.clear();
    }

    @Override
	public void setBufferTransform(wl_surface.Resource resource, int transform)
    {
        pendingBufferTransform = transform;
    }
}

