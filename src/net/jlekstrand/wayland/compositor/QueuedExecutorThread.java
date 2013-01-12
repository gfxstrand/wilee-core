package net.jlekstrand.wayland.compositor;

import java.util.ArrayDeque;

class QueuedExecutorThread extends Thread implements QueuedExecutor
{
    private enum State {
        RUNNING,
        FINISHING,
        STOPPED
    };

    private ArrayDeque<Runnable> jobQueue;
    private State state;

    public QueuedExecutorThread()
    {
        jobQueue = new ArrayDeque<Runnable>();
        state = State.STOPPED;
    }

    public void run()
    {
        state = State.RUNNING;

        while (state != State.STOPPED) {
            Runnable job = null;
            synchronized (jobQueue)
            {
                while(jobQueue.isEmpty() && state == State.RUNNING) {
                    try {
                        jobQueue.wait();
                    } catch (InterruptedException e) {
                    }
                }

                if (jobQueue.isEmpty() && state == State.FINISHING) {
                    state = State.STOPPED;
                    return;
                } else if (jobQueue.isEmpty() || state == State.STOPPED) {
                    throw new IllegalStateException();
                }

                job = jobQueue.peek();
            }

            job.run();

            synchronized (jobQueue)
            {
                jobQueue.poll();
            }
        }
    }

    public void finished()
    {
        synchronized (jobQueue)
        {
            state = State.FINISHING;
            jobQueue.notifyAll();
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

