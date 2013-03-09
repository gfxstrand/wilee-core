package net.jlekstrand.wheatley;

import android.graphics.Region;

import org.freedesktop.wayland.server.Global;
import org.freedesktop.wayland.protocol.wl_shell;

interface Shell extends wl_shell.Requests
{
    public abstract void render(Renderer renderer);
    public abstract boolean surfaceDamaged(Surface surface, Region damage);
}

