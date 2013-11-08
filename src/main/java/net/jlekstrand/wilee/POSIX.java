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

import org.freedesktop.wayland.arch.Native;

import java.io.IOException;

public class POSIX
{
    public native static void pipe(int pipefd[]) throws IOException;
    public native static void close(int fd) throws IOException;
    public native static void read(int fd, byte buf[], long count)
            throws IOException;
    public static void read(int fd, byte buf[]) throws IOException
    {
        read(fd, buf, buf.length);
    }
    public native static void write(int fd, byte buf[], long count)
            throws IOException;
    public static void write(int fd, byte buf[]) throws IOException
    {
        write(fd, buf, buf.length);
    }
    private native static void _setenv(byte name[], byte value[],
            boolean overwrite);
    public static void setenv(String name, String value, boolean overwrite)
    {
        _setenv(name.getBytes(), value.getBytes(), overwrite);
    }

    static {
        Native.loadLibrary("wilee-core");
    }
}

