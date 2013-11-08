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
import org.freedesktop.wayland.protocol.wl_buffer;

public class ShmBuffer extends Buffer
{
    private final int offset;
    private final int stride;
    private final int format;

    private final ShmPool pool;

    public ShmBuffer(Client client, int id, ShmPool pool, int offset,
            int width, int height, int stride, int format)
    {
        super(client, id, width, height);

        this.offset = offset;
        this.stride = stride;
        this.format = format;
        this.pool = pool;
    }

    public ByteBuffer getBuffer()
    {
        return pool.getBuffer();
    }

    public int getStride()
    {
        return stride;
    }

    public int getFormat()
    {
        return format;
    }

    @Override
    public void destroy(wl_buffer.Resource resource)
    {
        pool.release();
        super.destroy(resource);
    }
}

