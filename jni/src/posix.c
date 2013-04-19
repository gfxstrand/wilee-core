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
#include <jni.h>

#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>

#include "jni_util.h"

#ifdef ANDROID
#   include <android/log.h>
#   define LOG_DEBUG(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "wheatley-core", __VA_ARGS__))
#else
#   define LOG_DEBUG(...)
#endif

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_POSIX_pipe(JNIEnv * env, jclass clazz,
        jintArray jpipefd)
{
    int pipefd[2];
    int success;
    int flags;

    if (jpipefd == NULL) {
        jni_util_throw_by_name(env, "java/lang/NullPointerException", NULL);
        return;
    }

    if ((*env)->GetArrayLength(env, jpipefd) < 2) {
        jni_util_throw_by_name(env, "java/lang/ArrayIndexOutOfBoundsException",
                "pipe expects at least 2 integers");
        return;
    }

    success = pipe(pipefd);
    if (success == -1) {
        jni_util_throw_by_name(env, "java/io/IOException", strerror(errno));
        return;
    }

    flags = fcntl(pipefd[0], F_GETFD);
    if (flags == -1) {
        jni_util_throw_by_name(env, "java/io/IOException", strerror(errno));
        goto error;
    }
    success = fcntl(pipefd[0], F_SETFD, flags |= FD_CLOEXEC);
    if (success == -1) {
        jni_util_throw_by_name(env, "java/io/IOException", strerror(errno));
        goto error;
    }

    flags = fcntl(pipefd[1], F_GETFD);
    if (flags == -1) {
        jni_util_throw_by_name(env, "java/io/IOException", strerror(errno));
        goto error;
    }
    success = fcntl(pipefd[1], F_SETFD, flags |= FD_CLOEXEC);
    if (success == -1) {
        jni_util_throw_by_name(env, "java/io/IOException", strerror(errno));
        goto error;
    }
    
    (*env)->SetIntArrayRegion(env, jpipefd, 0, 2, pipefd);
    if ((*env)->ExceptionCheck(env) == JNI_TRUE) {
        goto error;
    }

    return;

error:
    close(pipefd[0]);
    close(pipefd[1]);
}

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_POSIX_close(JNIEnv * env, jclass clazz, int fd)
{
    if (close(fd) == -1)
        jni_util_throw_by_name(env, "java/io/IOException", strerror(errno));
}

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_POSIX_read(JNIEnv * env, jclass clazz, int fd,
        jbyteArray jbuff, jlong count)
{
    ssize_t size;
    jbyte *buff;

    if (jbuff == NULL) {
        jni_util_throw_by_name(env, "java/lang/NullPointerException", NULL);
        return;
    }

    if (count > (*env)->GetArrayLength(env, jbuff)) {
        jni_util_throw_by_name(env, "java/lang/ArrayIndexOutOfBoundsException",
                NULL);
        return;
    }

    buff = (*env)->GetByteArrayElements(env, jbuff, NULL);
    if (buff == NULL)
        return; /* Exception Thrown */

    size = read(fd, buff, count);
    if (size != 1)
        jni_util_throw_by_name(env, "java/io/IOException", strerror(errno));

    (*env)->ReleaseByteArrayElements(env, jbuff, buff, 0);
}

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_POSIX_write(JNIEnv * env, jclass clazz, int fd,
        jbyteArray jbuff, jlong count)
{
    ssize_t size;
    jbyte *buff;

    if (jbuff == NULL) {
        jni_util_throw_by_name(env, "java/lang/NullPointerException", NULL);
        return;
    }

    if (count > (*env)->GetArrayLength(env, jbuff)) {
        jni_util_throw_by_name(env, "java/lang/ArrayIndexOutOfBoundsException",
                NULL);
        return;
    }

    buff = (*env)->GetByteArrayElements(env, jbuff, NULL);
    if (buff == NULL)
        return; /* Exception Thrown */

    size = write(fd, buff, count);
    if (size != 1)
        jni_util_throw_by_name(env, "java/io/IOException", strerror(errno));

    (*env)->ReleaseByteArrayElements(env, jbuff, buff, JNI_ABORT);
}

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_POSIX__1setenv(JNIEnv * env, jclass clazz,
        jbyteArray jname, jbyteArray jvalue, jboolean overwrite)
{
    jsize name_len, value_len;
    jbyte *name, *value;

    if (jname == NULL || jvalue == NULL) {
        jni_util_throw_by_name(env, "java/lang/NullPointerException", NULL);
        return;
    }

    name_len = (*env)->GetArrayLength(env, jname);
    value_len = (*env)->GetArrayLength(env, jvalue);
    if ((*env)->ExceptionCheck(env))
        return;

    name = malloc((name_len + 1) * sizeof *name);
    if (name == NULL) {
        jni_util_throw_by_name(env, "java/lang/OutOfMemoryError", NULL);
        return;
    }
    value = malloc((value_len + 1) * sizeof *value);
    if (value == NULL) {
        jni_util_throw_by_name(env, "java/lang/OutOfMemoryError", NULL);
        goto free_arrays;
    }

    (*env)->GetByteArrayRegion(env, jname, 0, name_len, name);
    if ((*env)->ExceptionCheck(env))
        goto free_arrays;
    (*env)->GetByteArrayRegion(env, jvalue, 0, value_len, value);
    if ((*env)->ExceptionCheck(env))
        goto free_arrays;

    name[name_len] = '\0';
    value[value_len] = '\0';

    if (setenv(name, value, overwrite) < 0) {
        switch (errno) {
        case ENOMEM:
            jni_util_throw_by_name(env, "java/lang/OutOfMemoryError", NULL);
            break;
        case EINVAL:
            jni_util_throw_by_name(env, "java/lang/IllegalArgumentException",
                    NULL);
            break;
        default:
            jni_util_throw_by_name(env, "java/lang/RuntimeException",
                    strerror(errno));
            break;
        }
        goto free_arrays;
    }

free_arrays:
    free(value);
    free(name);
}

