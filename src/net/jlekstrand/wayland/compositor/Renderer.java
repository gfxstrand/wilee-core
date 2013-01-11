package net.jlekstrand.wayland.compositor;

import android.view.SurfaceHolder;

public interface Renderer extends SurfaceHolder.Callback
{
    public abstract void render(Shell shell);
}

