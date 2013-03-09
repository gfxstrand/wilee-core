#include <jni.h>

#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>

#include "jni_util.h"

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_POSIX_pipe(JNIEnv * env, jclass clazz,
        jintArray jpipefd)
{
    int pipefd[2];
    int success;
    int flags;

    if (pipefd == NULL) {
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
Java_net_jlekstrand_wheatley_POSIX_Read(JNIEnv * env, jclass clazz, int fd,
        jbyteArray jbuff, jlong count)
{
    ssize_t size;
    jbyte *buff;

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

