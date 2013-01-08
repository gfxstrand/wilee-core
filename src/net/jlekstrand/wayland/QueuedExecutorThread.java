package net.jlekstrand.wayland;

import java.util.ArrayDeque;

class QueuedExecutorThread extends Thread implements QueuedExecutor
{
    ArrayDeque<Runnable> jobQueue;
    boolean running = true;

    public QueuedExecutorThread()
    {
        jobQueue = new ArrayDeque<Runnable>();
    }

    public void run()
    {
        running = true;

        while (running) {
            Runnable job = null;
            synchronized (jobQueue)
            {
                while(jobQueue.isEmpty() && running) {
                    try {
                        jobQueue.wait();
                    } catch (InterruptedException e) {
                    }
                }

                if (! running)
                    return;

                job = jobQueue.peek();
            }

            job.run();

            synchronized (jobQueue)
            {
                jobQueue.poll();
                jobQueue.notifyAll();
            }
        }
    }

    public void waitForEmpty() throws InterruptedException
    {
        synchronized (jobQueue)
        {
            while (! jobQueue.isEmpty())
                jobQueue.wait();
        }
    }

    public void execute(Runnable c)
    {
        synchronized (jobQueue)
        {
            jobQueue.offer(c);
            jobQueue.notifyAll();
        }
    }
}

