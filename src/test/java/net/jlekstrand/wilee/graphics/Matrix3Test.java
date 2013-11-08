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

import java.util.Random;

import org.junit.*;

public class Matrix3Test
{
    @Test
    public void mult()
    {
        Matrix3 matA = new Matrix3(new float[] {
            0, 1, 3,
            1, -5, -2,
            1, 2, 4
        }, true);

        Matrix3 matB = new Matrix3(new float[] {
            0, 0.5f, -2,
            -2, 3, 2,
            5, -1, 0
        }, true);

        Matrix3 matAB = matA.mult(matB);
        Assert.assertTrue(matAB.approxEqual(new Matrix3(new float[] {
            13, 0, 2,
            0, -12.5f, -12,
            16, 2.5f, 2
        }, true)));

        Matrix3 matBA = matB.mult(matA);
        Assert.assertTrue(matBA.approxEqual(new Matrix3(new float[] {
            -1.5f, -6.5f, -9,
            5, -13, -4,
            -1, 10, 17
        }, true)));
    }

    @Test
    public void invert()
    {
        Matrix3 mat = new Matrix3(new float[] {
            1, 2, 3,
            0, 1, 4,
            5, 6, 0
        }, true);

        Assert.assertTrue(mat.inverse().approxEqual(new Matrix3(new float[] {
            -24, 18, 5,
            20, -15, -4,
            -5, 4, 1
        }, true)));

        Random random = new Random();
        for (int i = 0; i < 5; ++i) {
            mat = new Matrix3(new float[] {
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat(),
                random.nextFloat()
            });
            Matrix3 invMat = mat.inverse();

            Assert.assertTrue(mat.mult(invMat).approxEqual(Matrix3.identity()));
        }
    }
}

