package net.jlekstrand.wheatley;

public interface Renderer
{
    public abstract void beginRender(boolean clear);
    public abstract int endRender();

    public abstract void drawSurface(Surface surface);
}

