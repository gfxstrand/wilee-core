package net.jlekstrand.wheatley;

import java.util.concurrent.Executor;

interface QueuedExecutor extends Executor
{
    public abstract void finished();
}

