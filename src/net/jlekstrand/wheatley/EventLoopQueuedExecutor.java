package net.jlekstrand.wheatley;

import java.io.IOException;
import java.util.ArrayDeque;

import org.freedesktop.wayland.server.EventLoop;

class EventLoopQueuedExecutor
        implements QueuedExecutor, EventLoop.FileDescriptorEventHandler
{
    private static final byte EVENT_NEW_JOB = 1;
    private static final byte EVENT_FINISHED = 0;

    private ArrayDeque<Runnable> jobQueue;
    private boolean running;

    private int pipefd[];

    private EventLoop eventLoop;
    private EventLoop.EventSource eventSource;

    public EventLoopQueuedExecutor() throws IOException
    {
        jobQueue = new ArrayDeque<Runnable>();
        eventSource = null;
        running = false;

        pipefd = new int[2];
        POSIX.pipe(pipefd);
    }

    public void addToEventLoop(EventLoop loop) throws IOException
    {
        synchronized (jobQueue) {
            running = true;
            eventLoop = loop;
        }

        eventSource = eventLoop.addFileDescriptor(pipefd[0],
                EventLoop.EVENT_READABLE, this);
        if (eventSource == null)
            throw new NullPointerException();
    }

    private void cleanup()
    {
        synchronized (jobQueue) {
            running = false;

            try {
                POSIX.close(pipefd[0]);
            } catch (IOException e) {
            }

            try {
                POSIX.close(pipefd[1]);
            } catch (IOException e) {
            }

            eventLoop.remove(eventSource);
            eventSource = null;
        }
    }

    private void handleEvent(byte event)
    {
        if (event == EVENT_FINISHED) {
            cleanup();
        } else if (event == EVENT_NEW_JOB) {
            Runnable job = null;

            synchronized (jobQueue) {
                if (jobQueue.isEmpty())
                    throw new IllegalStateException();

                job = jobQueue.peek();
            }

            job.run();

            synchronized (jobQueue) {
                jobQueue.poll();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public int handleFileDescriptorEvent(int fd, int mask)
    {
        try {
            byte event[] = new byte[1];
            POSIX.read(pipefd[0], event);

            while (true) {
                handleEvent(event[0]);

                synchronized (jobQueue) {
                    if (! jobQueue.isEmpty()) {
                        POSIX.read(pipefd[0], event);
                    } else {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            cleanup();
        }

        return 0;
    }

    public void execute(Runnable job)
    {
        try {
            synchronized (jobQueue) {
                if (! running)
                    return;

                jobQueue.offer(job);
                byte b[] = { EVENT_NEW_JOB };
                POSIX.write(pipefd[1], b, 1);
            }
        } catch (IOException e) {
            cleanup();
        }
    }

    public void finished()
    {
        try {
            synchronized (jobQueue) {
                if (! running)
                    return;

                running = false;
                byte b[] = { EVENT_FINISHED };
                POSIX.write(pipefd[1], b, 1);
            }
        } catch (IOException e) {
            cleanup();
        }
    }
}

