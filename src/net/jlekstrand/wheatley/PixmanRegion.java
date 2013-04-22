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

final class PixmanRegion
{
    static native long create();
    static native long create_rect(int x, int y, int width, int height);
    static native long clone(long reg);
    static native void destroy(long ptr);
    static native void translate(long ptr, int x, int y);
    static native void copy(long dest, long src);
    static native void intersect(long new_reg, long reg1, long reg2);
    static native void union(long new_reg, long reg1, long reg2);
    static native void union_rect(long dest, long source,
            int x, int y, int width, int height);
    static native void intersect_rect(long dest, long source,
            int x, int y, int width, int height);
    static native void subtract(long reg_d, long reg_m, long reg_s);
    static native void inverse(long dest, long src,
            int x1, int y1, int x2, int y2);
    static native boolean contains_point(long reg, int x, int y);
    static native int n_rects(long reg);
    static native Rect get_rect(long region, int idx);
    static native boolean equal(long reg1, long reg2);

    private static native void initializeJNI();

    static {
        System.loadLibrary("pixman-1");
        System.loadLibrary("wheatley-core");
        initializeJNI();
    }
}

