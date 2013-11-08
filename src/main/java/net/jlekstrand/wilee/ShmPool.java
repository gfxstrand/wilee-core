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

import java.nio.ByteBuffer;

import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Resource;

import org.freedesktop.wayland.protocol.wl_shm_pool;

public class ShmPool implements wl_shm_pool.Requests
{
    public final wl_shm_pool.Resource resource;
    private int refCount;
    private org.freedesktop.wayland.ShmPool pool;

    public ShmPool(Client client, int id, int fd, int size)
    {
        resource = new wl_shm_pool.Resource(client, 1, id);
        resource.setImplementation(this);

        this.refCount = 1;
        try {
            this.pool = org.freedesktop.wayland.ShmPool.fromFileDescriptor(
                    fd, size, false, true);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ByteBuffer getBuffer()
    {
        return pool.asByteBuffer();
    }

    @Override
	public void createBuffer(wl_shm_pool.Resource resource, int id,
            int offset, int width, int height, int stride, int format)
    {
        if (width < 0 || height < 0 || stride < 0 || offset < 0)
            throw new ArrayIndexOutOfBoundsException();

        if (stride < width * 4 || height * stride + offset > pool.size())
            throw new ArrayIndexOutOfBoundsException();

        // Yeah, there's no error checking yet... That needs to be fixed
        new ShmBuffer(resource.getClient(), id, this, offset, width, height,
                stride, format);

        ++refCount;
    }

    public void release()
    {
        --refCount;

        if (refCount == 0 && pool != null) {
            try {
                pool.close();
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
            pool = null;
        }
    }

    @Override
    public void destroy(wl_shm_pool.Resource resource)
    {
        release();
        resource.destroy();
    }

    @Override
	public void resize(wl_shm_pool.Resource resource, int size)
    {
        try {
            pool.resize(size);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}

