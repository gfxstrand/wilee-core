package net.jlekstrand.wayland.compositor;

import java.util.concurrent.Executor;

interface QueuedExecutor extends Executor
{
    public abstract void finished();
}

