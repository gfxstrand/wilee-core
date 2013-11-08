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
package net.jlekstrand.wilee;

import net.jlekstrand.wilee.graphics.Point;
import net.jlekstrand.wilee.graphics.Matrix3;

import org.freedesktop.wayland.Fixed;
import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.DestroyListener;
import org.freedesktop.wayland.server.Client;

import org.freedesktop.wayland.protocol.wl_pointer;

public class Pointer implements wl_pointer.Requests
{
    final Seat seat;

    public static final int BTN_MOUSE = 0x110;
    private static final int MAX_DRAG_BUTTONS = 5;

    private static final String LOG_TAG = "Pointer";
    
    private Point currentPos;
    private Surface focus;
    private wl_pointer.Resource focusResource;
    private DestroyListener focusDestroyListener = new DestroyListener() {
        public void onDestroy()
        {
            focusResource = null;
        }
    };
    private int lastSerial;
    private int lastButton;
    private Seat.DragListener dragListener;
    private int dragButton;

    private final ClientResourceMap resources;

    public Pointer(Seat seat)
    {
        this.seat = seat;
        resources = new ClientResourceMap();
    }

    public void bindClient(Client client, int id)
    {
        wl_pointer.Resource resource = new wl_pointer.Resource(client, 1, id);
        resource.setImplementation(this);
        resources.addResource(resource);
    }

    @Override
    public void setCursor(wl_pointer.Resource resource, int serial,
            Resource surface, int hotspot_x, int hotspot_y)
    { }

    public void setFocus(Surface newFocus, Point globalPos)
    {
        if (focus == newFocus)
            return;

        Log.d(LOG_TAG, "Pointer focus changed");

        if (focusResource != null) {
            focusResource.leave(seat.compositor.display.nextSerial(),
                    focus.getResource());
            focusDestroyListener.detach();
        }

        if (dragListener != null)
            dragListener.setFocus(newFocus, globalPos);

        if (newFocus != null && newFocus.getResource() != null) {
            focus = newFocus;
            focusResource = (wl_pointer.Resource)resources.getResource(
                    newFocus.getResource().getClient());

            if (focusResource != null) {
                focusResource.addDestroyListener(focusDestroyListener);
                Point p = focus.fromGlobalCoordinates(globalPos);
                focusResource.enter(seat.compositor.display.nextSerial(),
                        focus.getResource(),
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
        this.currentPos = pos;

        Surface newFocus = seat.compositor.findSurfaceAt(pos);
        if (focus != newFocus)
            setFocus(newFocus, pos);

        if (focusResource != null) {
            Point p = focus.fromGlobalCoordinates(pos);
            focusResource.motion(time, new Fixed(p.getX()),
                    new Fixed(p.getY()));

            if (dragListener != null)
                dragListener.motion(time, p);
        }
    }

    public void handleButton(int serial, int time, int button, int state)
    {
        if (focusResource != null) {
            if (state == wl_pointer.BUTTON_STATE_PRESSED) {
                lastSerial = serial;
                lastButton = button;
            }

            focusResource.button(serial, time, button, state);

            if (state == wl_pointer.BUTTON_STATE_RELEASED) {
                if (dragListener != null) {
                    dragListener.drop();
                    dragListener = null;
                }
            }
        }
    }

    boolean requestDrag(Seat.DragListener dragListener, Surface surface,
            int serial)
    {
        if (focus != surface)
            return false;

        if (lastSerial != serial)
            return false;

        this.dragButton = lastButton;
        this.dragListener = dragListener;

        dragListener.setFocus(surface, currentPos);

        return true;
    }

    public void handleAxis(int time, int axis, Fixed value)
    {
        if (focusResource != null) {
            focusResource.axis(time, axis, value);
        }
    }
}

