package net.jlekstrand.wheatley;

public abstract class AbstractThreadedRenderer
        implements Renderer
{
    protected static abstract class SafeHandoffRunnable implements Runnable
    {
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

    protected QueuedExecutorThread renderThread;

    protected void onBeginRender(boolean clear)
    {
        return;
    }

    protected int onEndRender()
    {
        return (int)java.lang.System.currentTimeMillis();
    }

    protected abstract void onDrawSurface(Surface surface);

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
}

