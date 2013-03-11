package net.jlekstrand.wheatley;

import java.io.Closeable;

public abstract class AbstractThreadedRenderer
        implements Renderer, Closeable
{
    protected static class SafeHandoffRunnable implements Runnable
    {
        boolean finished;
        java.lang.Throwable error;
        Object returnValue;
        Runnable client;

        public SafeHandoffRunnable(Runnable client)
        {
            this.client = client;
            finished = false;
            error = null;
            returnValue = null;
        }

        @Override
        public final void run()
        {
            java.lang.Throwable cachedError;
            try {
                client.run();
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

        public final synchronized void waitForHandoff()
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
        }

    }

    private QueuedExecutorThread renderThread;

    public AbstractThreadedRenderer()
    {
        renderThread = new QueuedExecutorThread();
        renderThread.start();
    }

    protected void execute(Runnable r)
    {
        if (renderThread == null)
            throw new IllegalStateException();

        SafeHandoffRunnable closure = new SafeHandoffRunnable(r);
        renderThread.execute(closure);
        closure.waitForHandoff();
    }

    protected void executeAndClose(Runnable r)
    {
        if (renderThread == null)
            throw new IllegalStateException();

        QueuedExecutorThread tmpThread = renderThread;
        renderThread = null;

        SafeHandoffRunnable closure = new SafeHandoffRunnable(r);
        tmpThread.execute(closure);
        tmpThread.finished();

        closure.waitForHandoff();
    }

    @Override
    public void close()
    {
        if (renderThread != null) {
            renderThread.finished();
            renderThread = null;
        }
    }

    @Override
    public void finalize() throws Throwable
    {
        close();
        super.finalize();
    }

    @Override
    public final void beginRender(final boolean clear)
    {
        execute(new Runnable() {
            public void run()
            {
                onBeginRender(clear);
            }
        });
    }

    @Override
    public final int endRender()
    {
        final int retval[] = new int[1];

        execute(new Runnable() {
            public void run()
            {
                retval[0] = onEndRender();
            }
        });

        return retval[0];
    }

    @Override
    public final void drawSurface(final Surface surface)
    {
        execute(new Runnable() {
            public void run()
            {
                onDrawSurface(surface);
            }
        });
    }

    protected void onBeginRender(boolean clear)
    {
        return;
    }

    protected int onEndRender()
    {
        return (int)java.lang.System.currentTimeMillis();
    }

    protected abstract void onDrawSurface(Surface surface);
}

