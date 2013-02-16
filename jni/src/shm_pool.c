#include <jni.h>

#include <errno.h>
#include <sys/mman.h>

JNIEXPORT jobject JNICALL
Java_net_jlekstrand_wayland_compositor_ShmPool_map(JNIEnv * env, jclass clazz,
        int fd, int size)
{
    void * buffer;
    
    buffer = mmap(NULL, size, PROT_READ, MAP_SHARED, fd, 0);

    if (buffer == MAP_FAILED) {
        jni_util_throw_by_name(env, "java/io/IOException", strerror(errno));
        return NULL;
    }

    return (*env)->NewDirectByteBuffer(env, buffer, size);
}

JNIEXPORT jobject JNICALL
Java_net_jlekstrand_wayland_compositor_ShmPool_unmap(JNIEnv * env,
        jclass clazz, jobject buffer)
{
    void * data;
    int size, success;

    // TODO: Error Checking

    data = (*env)->GetDirectBufferAddress(env, buffer);
    size = (*env)->GetDirectBufferCapacity(env, buffer);

    success = munmap(data, size);
    if (success < 0)
        jni_util_throw_by_name(env, "java/io/IOException", strerror(errno));
}

