/*
 * Copyright © 2012-2013 Jason Ekstrand.
 *  
 * Permission to use, copy, modify, distribute, and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation, and
 * that the name of the copyright holders not be used in advertising or
 * publicity pertaining to distribution of the software without specific,
 * written prior permission.  The copyright holders make no representations
 * about the suitability of this software for any purpose.  It is provided "as
 * is" without express or implied warranty.
 * 
 * THE COPYRIGHT HOLDERS DISCLAIM ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,
 * INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO
 * EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY SPECIAL, INDIRECT OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE,
 * DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE
 * OF THIS SOFTWARE.
 */
package net.jlekstrand.wheatley;

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

