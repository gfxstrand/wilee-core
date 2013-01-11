package net.jlekstrand.wayland.compositor;

import android.view.SurfaceHolder;

abstract class AbstractRenderer implements Renderer
{
    private static abstract class SafeHandoffRunnable implements Runnable{
        boolean finished;
        java.lang.Error error;

        public SafeHandoffRunnable()
        {
            error = null;
            finished = false;
        }

        public abstract void onRun();

        @Override
        public final void run()
        {
            java.lang.Error cachedError;
            try {
                onRun();
                cachedError = null;
            } catch (java.lang.Error e) {
                cachedError = e;
            }

            synchronized (this) {
                error = cachedError;
                finished = true;
                this.notifyAll();
            }
        }

        public final synchronized void waitForHandoff()
        {
            while (! finished) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                }
            }

            if (error != null)
                throw error;
        }

    }

    QueuedExecutorThread renderThread;

    protected abstract void onDrawSurface(Surface surface);

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
    public final void drawSurface(final Surface surface)
    {
        if (renderThread == null)
            return;

        SafeHandoffRunnable closure = new SafeHandoffRunnable() {
            public void onRun()
            {
                onDrawSurface(surface);
            }
        };

        renderThread.execute(closure);
        closure.waitForHandoff();
    }

    @Override
    public final void render(final Shell shell)
    {
        if (renderThread == null)
            return;

        SafeHandoffRunnable closure = new SafeHandoffRunnable() {
            public void onRun()
            {
                onRender(shell);
            }
        };

        renderThread.execute(closure);
        closure.waitForHandoff();
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

        SafeHandoffRunnable closure = new SafeHandoffRunnable() {
            public void onRun()
            {
                onSurfaceChanged(holder, format, width, height);
            }
        };

        renderThread.execute(closure);
        closure.waitForHandoff();
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

        SafeHandoffRunnable closure = new SafeHandoffRunnable() {
            public void onRun()
            {
                onSurfaceDestroyed(holder);
            }
        };

        tmpThread.execute(closure);
        tmpThread.finished();

        closure.waitForHandoff();
    }
}

