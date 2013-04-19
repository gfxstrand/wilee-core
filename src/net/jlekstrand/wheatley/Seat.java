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

import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Global;

import org.freedesktop.wayland.protocol.wl_seat;

public class Seat extends Global implements wl_seat.Requests
{
    public final Compositor compositor;

    public final Pointer pointer;
    public final Keyboard keyboard;
    public final TouchHandler touchHandler;

    private int capabilities;

    public Seat(Compositor compositor, int capabilities)
    {
        super(wl_seat.WAYLAND_INTERFACE);

        this.compositor = compositor;
        this.capabilities = capabilities;

        if ((capabilities & wl_seat.CAPABILITY_POINTER) != 0)
            pointer = new Pointer(this);
        else
            pointer = null;

        if ((capabilities & wl_seat.CAPABILITY_KEYBOARD) != 0)
            keyboard = new Keyboard(this);
        else
            keyboard = null;

        if ((capabilities & wl_seat.CAPABILITY_TOUCH) != 0)
            touchHandler = new TouchHandler(this);
        else
            touchHandler = null;
    }

    @Override
    public void bindClient(Client client, int version, int id)
    {
        wl_seat.Resource res = new wl_seat.Resource(client, id, this);
        res.capabilities(capabilities);
    }

    @Override
	public void getKeyboard(wl_seat.Resource resource, int id)
    {
        if (keyboard != null)
            keyboard.bindClient(resource.getClient(), id);
    }

    @Override
	public void getTouch(wl_seat.Resource resource, int id)
    {
        if (touchHandler != null)
            touchHandler.bindClient(resource.getClient(), id);
    }

    @Override
    public void getPointer(wl_seat.Resource resource, int id)
    {
        if (pointer != null)
            pointer.bindClient(resource.getClient(), id);
    }
}

