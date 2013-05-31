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

import java.io.PrintStream;

public final class Log
{
    public interface Logger
    {
        public void logMessage(int lvl, String tag, String message);
    }

    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARNING = 5;
    public static final int ERROR = 6;

    public static class DefaultLogger implements Logger
    {
        public void logMessage(int level, String tag, String message)
        {
            final PrintStream stream;
            switch (level) {
            case ERROR:
            case WARNING:
                stream = System.err;
                break;
            default:
                stream = System.out;
                break;
            }

            switch (level) {
            case ERROR:
                stream.print("ERROR ");
                break;
            case WARNING:
                stream.print("WARNING ");
                break;
            case DEBUG:
            case VERBOSE:
                stream.print("DEBUG ");
                break;
            }

            stream.print(tag);
            stream.print(": ");
            stream.println(message);
        }
    }

    private static Logger currentLogger = new DefaultLogger();
    private static int currentLevel = INFO;

    public static void setLevel(int level)
    {
        if (level > 6 || level < 0)
            throw new IllegalArgumentException("Invalid log level");
        
        currentLevel = level;
    }

    public static int getLevel()
    {
        return currentLevel;
    }

    public static void setLogger(Logger logger)
    {
        if (logger != null)
            currentLogger = logger;
        else
            currentLogger = new DefaultLogger();
    }

    private static void log(int level, String tag, String message)
    {
        if (level >= currentLevel)
            currentLogger.logMessage(level, tag, message);
    }

    public static void d(String tag, String message)
    {
        log(DEBUG, tag, message);
    }

    public static void e(String tag, String message)
    {
        log(ERROR, tag, message);
    }

    public static void i(String tag, String message)
    {
        log(INFO, tag, message);
    }

    public static void v(String tag, String message)
    {
        log(VERBOSE, tag, message);
    }

    public static void w(String tag, String message)
    {
        log(WARNING, tag, message);
    }
}

