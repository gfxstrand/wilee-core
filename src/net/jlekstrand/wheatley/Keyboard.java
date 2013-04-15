package net.jlekstrand.wheatley;

import org.freedesktop.wayland.server.Resource;
import org.freedesktop.wayland.server.Client;

import org.freedesktop.wayland.protocol.wl_keyboard;

public class Keyboard
{
    final Seat seat;
    final ClientResourceMap resources;

    public Keyboard(Seat seat)
    {
        this.seat = seat;
        resources = new ClientResourceMap();
    }

    public void bindClient(Client client, int id)
    { }
}

