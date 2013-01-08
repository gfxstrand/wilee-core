package net.jlekstrand.wayland;

import java.util.concurrent.Executor;

interface QueuedExecutor extends Executor
{
    public abstract void waitForEmpty() throws InterruptedException;
}

