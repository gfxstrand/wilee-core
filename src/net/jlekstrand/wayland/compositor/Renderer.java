package net.jlekstrand.wayland.compositor;

import android.view.SurfaceHolder;

public interface Renderer
{
    public abstract void beginRender(boolean clear);
    public abstract int endRender();

    public abstract void drawSurface(Surface surface);
}

