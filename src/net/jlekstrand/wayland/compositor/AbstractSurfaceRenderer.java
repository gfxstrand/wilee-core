package net.jlekstrand.wayland.compositor;

import android.view.SurfaceHolder;

abstract class AbstractSurfaceRenderer
        implements Renderer, SurfaceHolder.Callback
{
    private static abstract class SafeHandoffRunnable implements Runnable{
        boolean finished;
        java.lang.Throwable error;
        Object returnValue;

        public SafeHandoffRunnable()
        {
            finished = false;
            error = null;
            returnValue = null;
        }

        public abstract Object onRun();

        @Override
        public final void run()
        {
            java.lang.Throwable cachedError;
            try {
                returnValue = onRun();
                cachedError = null;
            } catch (java.lang.Error e) {
                cachedError = e;
            } catch (java.lang.RuntimeException e) {
                cachedError = e;
            }

            synchronized (this) {
                error = cachedError;
                finished = true;
                this.notifyAll();
            }
        }

        public final synchronized Object waitForHandoff()
        {
            while (! finished) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                }
            }

            if (error != null) {
                if (error instanceof java.lang.Error)
                    throw (java.lang.Error)error;

                if (error instanceof java.lang.RuntimeException)
                    throw (java.lang.RuntimeException)error;
            }

            return returnValue;
        }

    }

    QueuedExecutorThread renderThread;

    protected void onBeginRender(boolean clear)
    {
        return;
    }

    protected int onEndRender()
    {
        return (int)java.lang.System.currentTimeMillis();
    }

    protected abstract void onDrawSurface(Surface surface);

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
    public final void beginRender(final boolean clear)
    {
        if (renderThread == null)
            return;

        SafeHandoffRunnable closure = new SafeHandoffRunnable() {
            public Object onRun()
            {
                onBeginRender(clear);
                return null;
            }
        };

        renderThread.execute(closure);
        closure.waitForHandoff();
    }

    @Override
    public final int endRender()
    {
        if (renderThread == null)
            return 0;

        SafeHandoffRunnable closure = new SafeHandoffRunnable() {
            public Object onRun()
            {
                return onEndRender();
            }
        };

        renderThread.execute(closure);
        return (Integer)closure.waitForHandoff();
    }

    @Override
    public final void drawSurface(final Surface surface)
    {
        if (renderThread == null)
            return;

        SafeHandoffRunnable closure = new SafeHandoffRunnable() {
            public Object onRun()
            {
                onDrawSurface(surface);
                return null;
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
            public Object onRun()
            {
                onSurfaceChanged(holder, format, width, height);
                return null;
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
            public Object onRun()
            {
                onSurfaceDestroyed(holder);
                return null;
            }
        };

        tmpThread.execute(closure);
        tmpThread.finished();

        closure.waitForHandoff();
    }
}

