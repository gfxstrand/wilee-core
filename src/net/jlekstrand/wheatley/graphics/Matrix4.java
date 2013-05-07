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
package net.jlekstrand.wheatley.graphics;

import java.nio.FloatBuffer;

public final class Matrix4
{
    /** Matrix data; storred in column-major order. */
    final float[] data;

    private Matrix4(float[] data)
    {
        this.data = data;
    }

    public static Matrix4 zeroes()
    {
        return new Matrix4(new float[] {
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0
        });
    }

    public static Matrix4 identity()
    {
        return new Matrix4(new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        });
    }

    public static Matrix4 ortho(float l, float r, float b, float t, float n, float f)
    {
        return new Matrix4(new float[] {
            2/(r - l), 0, 0, 0,
            0, 2/(t - b), 0, 0,
            0, 0, -2/(f - n), 0,
            -(r + l)/(r - l), -(t + b)/(t - b), (f + n)/(f - n), 1
        });
    }

    public static Matrix4 translate(float x, float y, float z)
    {
        return new Matrix4(new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            x, y, z, 1
        });
    }

    public static Matrix4 scale(float x, float y, float z)
    {
        return new Matrix4(new float[] {
            x, 0, 0, 0,
            0, y, 0, 0,
            0, 0, z, 0,
            0, 0, 0, 1
        });
    }

    public static Matrix4 rotate(float angle, float x, float y, float z)
    {
        float c = (float)Math.cos(angle);
        float s = (float)Math.sin(angle);

        /* normalize the vector */
        float norm = (float)Math.sqrt(x * x + y * y + z * z);
        x = x / norm;
        y = y / norm;
        z = z / norm;

        return new Matrix4(new float[] {
            x*x * (1-c) + c, y*x * (1-c) + z*s, x*z * (1-c) - y*s, 0,
            x*y * (1-c) - z*s, y*y * (1-c) + c, y*z * (1-c) + x*s, 0,
            x*z * (1-c) + y*s, y*z * (1-c) - x*s, z*z * (1-c) + c, 0,
            0, 0, 0, 1
        });
    }

    public Matrix4 mult(Matrix4 other)
    {
        final Matrix4 out = zeroes();

        for (int j = 0; j < 4; ++j) {
            for (int k = 0; k < 4; ++k)
                for (int i = 0; i < 4; ++i)
                    out.data[j*4+i] += data[k*4+i] * other.data[j*4+k];
        }

        return out;
    }

    public FloatBuffer asBuffer()
    {
        return FloatBuffer.wrap(data);
    }
}

