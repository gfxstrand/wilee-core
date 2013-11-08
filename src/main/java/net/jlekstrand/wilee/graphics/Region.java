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
package net.jlekstrand.wilee.graphics;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class Region implements Iterable<Rect>
{
    final long region_ptr;

    private class IteratorImpl implements Iterator<Rect>
    {
        final int n_rects;
        int idx;

        IteratorImpl()
        {
            n_rects = PixmanRegion.n_rects(region_ptr);
            idx = 0;
        }

        @Override
        public boolean hasNext()
        {
            return idx < n_rects;
        }

        @Override
        public Rect next()
        {
            if (idx >= n_rects)
                throw new NoSuchElementException();

            return PixmanRegion.get_rect(region_ptr, idx++);
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    public Region()
    {
        region_ptr = PixmanRegion.create();
    }

    public Region(Region other)
    {
        region_ptr = PixmanRegion.clone(other.region_ptr);
    }

    public Region(Rect r)
    {
        region_ptr = PixmanRegion.create_rect(r.left, r.top, r.right - r.left,
                r.bottom - r.top);
    }

    public boolean contains(int x, int y)
    {
        return PixmanRegion.contains_point(region_ptr, x, y);
    }

    public Region add(Rect r)
    {
        Region new_reg = new Region();
        PixmanRegion.union_rect(new_reg.region_ptr, region_ptr, r.left, r.top,
                r.right - r.left, r.bottom - r.top);
        return new_reg;
    }

    public Region add(Region r)
    {
        Region new_reg = new Region();
        PixmanRegion.union(new_reg.region_ptr, region_ptr, r.region_ptr);
        return new_reg;
    }

    public Region subtract(Rect r)
    {
        return subtract(new Region(r));
    }

    public Region subtract(Region r)
    {
        Region new_reg = new Region();
        PixmanRegion.subtract(new_reg.region_ptr, region_ptr, r.region_ptr);
        return new_reg;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;

        if (! (other instanceof Region))
            return false;

        return PixmanRegion.equal(region_ptr, ((Region)other).region_ptr);
    }

    @Override
    public Iterator<Rect> iterator()
    {
        return new IteratorImpl();
    }

    @Override
    public void finalize() throws Throwable
    {
        PixmanRegion.destroy(region_ptr);

        super.finalize();
    }
}

