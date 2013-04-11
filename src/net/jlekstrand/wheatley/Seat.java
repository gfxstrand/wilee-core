package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.Client;
import org.freedesktop.wayland.server.Global;

import org.freedesktop.wayland.protocol.wl_seat;

public class Seat extends Global implements wl_seat.Requests
{
    final Pointer pointer;
    final Keyboard keyboard;
    final TouchHandler touchHandler;

    private int capabilities;

    public Seat(int capabilities)
    {
        super(wl_seat.WAYLAND_INTERFACE);

        this.capabilities = capabilities;

        if ((capabilities & wl_seat.CAPABILITY_POINTER) != 0)
            pointer = new Pointer();
        else
            pointer = null;

        if ((capabilities & wl_seat.CAPABILITY_KEYBOARD) != 0)
            keyboard = new Keyboard();
        else
            keyboard = null;

        if ((capabilities & wl_seat.CAPABILITY_TOUCH) != 0)
            touchHandler = new TouchHandler();
        else
            touchHandler = null;
    }

    @Override
	public void getKeyboard(Resource resource, int id)
    {
        if (keyboard != null)
            keyboard.bindClient(resource.getClient(), id);
    }

    @Override
	public void getTouch(Resource resource, int id)
    {
        if (touchHandler != null)
            touchHandler.bindClient(resource.getClient(), id);
    }

    @Override
    public void getPointer(Resource resource, int id)
    {
        if (pointer != null)
            pointer.bindClient(resource.getClient(), id);
    }
}

