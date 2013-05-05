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

import java.nio.FloatBuffer;

public class Matrix3
{
    /* matrix data.  storred in column-major order */
    private final float[] data;

    private Matrix3(float[] data)
    {
        this.data = data;
    }

    public static Matrix3 zeroes()
    {
        return new Matrix3(new float[] {
            0, 0, 0,
            0, 0, 0,
            0, 0, 0
        });
    }

    public static Matrix3 identity()
    {
        return new Matrix3(new float[] {
            1, 0, 0,
            0, 1, 0,
            0, 0, 1
        });
    }

    public static Matrix3 translate(float x, float y)
    {
        return new Matrix3(new float[] {
            1, 0, 0,
            0, 1, 0,
            x, y, 1
        });
    }

    public static Matrix3 scale(float x, float y)
    {
        return new Matrix3(new float[] {
            x, 0, 0,
            0, y, 0,
            0, 0, 1
        });
    }

    public static Matrix3 rotate(float angle)
    {
        float s = (float)Math.sin(angle);
        float c = (float)Math.cos(angle);

        return new Matrix3(new float[] {
            c, s, 0,
            -s, c, 0,
            0, 0, 1
        });
    }

    public Matrix3 mult(Matrix3 other)
    {
        final Matrix3 out = zeroes();

        for (int j = 0; j < 3; ++j) {
            for (int k = 0; k < 3; ++k)
                for (int i = 0; i < 3; ++i)
                    out.data[j*3+i] += data[k*3+i] * other.data[j*3+k];
        }

        return out;
    }

    public FloatBuffer asBuffer()
    {
        return FloatBuffer.wrap(data);
    }
}

