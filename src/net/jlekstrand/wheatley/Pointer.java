/* Copyright © 2012-2013 Jason Ekstrand.
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

import net.jlekstrand.wheatley.graphics.Point;
import net.jlekstrand.wheatley.graphics.Matrix3;

import org.freedesktop.wayland.Fixed;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Client;

import org.freedesktop.wayland.protocol.wl_pointer;

public class Pointer implements wl_pointer.Requests
{
    final Seat seat;
    final ClientResourceMap resources;

    private static final String LOG_TAG = "Pointer";
    
    private Surface focus;
    private wl_pointer.Resource focusResource;
    private DestroyListener focusDestroyListener = new DestroyListener() {
        public void onDestroy()
        {
            focusResource = null;
        }
    };

    public Pointer(Seat seat)
    {
        this.seat = seat;
        resources = new ClientResourceMap();
    }

    public void bindClient(Client client, int id)
    {
        resources.addResource(new wl_pointer.Resource(client, id, this));
    }

    @Override
    public void setCursor(wl_pointer.Resource resource, int serial,
            Resource surface, Fixed hotspot_x, Fixed hotspot_y)
    { }

    public void setFocus(Surface newFocus, Point globalPos)
    {
        if (focus == newFocus)
            return;

        Log.d(LOG_TAG, "Pointer focus changed");

        if (focusResource != null) {
            focusResource.leave(seat.compositor.display.nextSerial(),
                    focus.resource);
            focusDestroyListener.detach();
        }

        if (newFocus != null) {
            focus = newFocus;
            focusResource = (wl_pointer.Resource)resources.getResource(
                    newFocus.resource.getClient());

            if (focusResource != null) {
                focusResource.addDestroyListener(focusDestroyListener);
                Point p = focus.fromGlobalCoordinates(globalPos);
                focusResource.enter(seat.compositor.display.nextSerial(),
                        focus.resource,
                        new Fixed(p.getX()), new Fixed(p.getY()));
            }
        } else {
            focus = null;
            focusResource = null;
        }
    }

    public Surface getFocus()
    {
        return focus;
    }

    public void handleMotion(int time, Point pos)
    {
        Surface newFocus = seat.compositor.findSurfaceAt(pos);
        if (focus != newFocus)
            setFocus(newFocus, pos);

        if (focusResource != null) {
            Point p = focus.fromGlobalCoordinates(pos);
            focusResource.motion(time, new Fixed(p.getX()),
                    new Fixed(p.getY()));
        }
    }

    public void handleButton(int serial, int time, int button, int state)
    {
        if (focusResource != null) {
            focusResource.button(seat.compositor.display.nextSerial(), time,
                    button, state);
        }
    }

    public void handleAxis(int time, int axis, Fixed value)
    {
        if (focusResource != null) {
            focusResource.axis(time, axis, value);
        }
    }
}

