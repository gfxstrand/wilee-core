#include <jni.h>

#include <sys/mman.h>

JNIEXPORT jobject JNICALL
Java_net_jlekstrand_wayland_ShmBuffer_map(JNIEnv * env, jclass clazz,
        int fd, int offset, int size)
{
    void * buffer;
    
    buffer = mmap(NULL, size, PROT_READ, MAP_SHARED, fd, offset);

    return (*env)->NewDirectByteBuffer(env, buffer, size);
}

JNIEXPORT jobject JNICALL
Java_net_jlekstrand_wayland_ShmBuffer_unmap(JNIEnv * env, jclass clazz,
        jobject buffer)
{
    void * data;
    int size;

    // TODO: Error Checking

    data = (*env)->GetDirectBufferAddress(env, buffer);
    size = (*env)->GetDirectBufferCapacity(env, buffer);

    munmap(data, size);
}

