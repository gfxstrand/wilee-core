package net.jlekstrand.wayland.compositor;

import android.view.Surface;
import android.view.SurfaceHolder;

class AbstractRenderer implements Renderer
{
    QueuedExecutorThread renderThread;

    protected void onRender(Shell shell)
    {
        return;
    }

    protected void onSurfaceCreated(SurfaceHolder holder)
    {
        return;
    }

    protected void onSurfaceChanged(SurfaceHolder holder, int format,
            int width, int height)
    {
        return;
    }

    protected void onSurfaceDestroyed(SurfaceHolder holder)
    {
        return;
    }

    @Override
    public final void render(final Shell shell)
    {
        if (renderThread == null)
            return;

        renderThread.execute(new Runnable() {
            public void run()
            {
                onRender(shell);
            }
        });

        while (true) {
            try {
                renderThread.waitForEmpty();
                break;
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public final void surfaceCreated(final SurfaceHolder holder)
    {
        renderThread = new QueuedExecutorThread();
        renderThread.start();

        renderThread.execute(new Runnable() {
            public void run() {
                onSurfaceCreated(holder);
            }
        });
    }

    @Override
    public final void surfaceChanged(final SurfaceHolder holder,
            final int format, final int width, final int height)
    {
        if (renderThread == null)
            return;

        renderThread.execute(new Runnable() {
            public void run() {
                onSurfaceChanged(holder, format, width, height);
            }
        });
    }

    @Override
    public final void surfaceDestroyed(final SurfaceHolder holder)
    {
        if (renderThread == null)
            return;

        // This makes sure our task is the last to get added to the render
        // thread
        QueuedExecutorThread tmpThread = renderThread;
        renderThread = null;

        tmpThread.execute(new Runnable() {
            public void run() {
                onSurfaceDestroyed(holder);
            }
        });

        while (true) {
            try {
                tmpThread.waitForEmpty();
                break;
            } catch (InterruptedException e) {
            }
        }
    }
}

