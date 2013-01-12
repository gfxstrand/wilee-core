package net.jlekstrand.wayland.compositor;

import java.io.IOException;
import java.util.ArrayDeque;

import android.util.Log;

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
        nativePipe(pipefd);
    }

    public void addToEventLoop(EventLoop loop) throws IOException
    {
        synchronized (jobQueue) {
            running = true;
            eventLoop = loop;
        }

        Log.d("EventLoopQueuedExecutor", "Testing Pipe (" + pipefd[0] + ", "
                + pipefd[1] + ")");
        nativeWrite(pipefd[1], (byte)7);
        byte b = nativeRead(pipefd[0]);
        if (b == (byte)7) {
            Log.d("EventLoopQueuedExecutor", "Pipe Test Successful");
        } else {
            Log.d("EventLoopQueuedExecutor", "Pipe Test Failed");
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
                nativeClose(pipefd[0]);
            } catch (IOException e) {
            }

            try {
                nativeClose(pipefd[1]);
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
        Log.d("EventLoopQueuedExecutor", "Handling Events");

        try {
            byte event = nativeRead(pipefd[0]);

            while (true) {
                handleEvent(event);

                synchronized (jobQueue) {
                    if (! jobQueue.isEmpty()) {
                        event = nativeRead(pipefd[0]);
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
                nativeWrite(pipefd[1], EVENT_NEW_JOB);
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
                nativeWrite(pipefd[1], EVENT_FINISHED);
            }
        } catch (IOException e) {
            cleanup();
        }
    }

    private native static void nativePipe(int pipefd[]) throws IOException;
    private native static void nativeClose(int fd) throws IOException;
    private native static byte nativeRead(int fd) throws IOException;
    private native static void nativeWrite(int fd, byte b) throws IOException;

    static {
        System.loadLibrary("wayland-app");
    }
}

