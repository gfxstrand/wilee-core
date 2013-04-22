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

#include "jni_util.h"

#include <stdlib.h>
#include <pixman.h>

static struct {
    jclass class;

    jmethodID init;

    jfieldID left;
    jfieldID top;
    jfieldID right;
    jfieldID bottom;
} Rect;

static jclass OutOfMemoryError;

static void
handle_error(JNIEnv *env, pixman_region16_t *reg)
{
    if (reg->data == NULL)
        (*env)->ThrowNew(env, OutOfMemoryError, NULL);
    else
        jni_util_throw_by_name(env, "java/lang/IllegalStateException",
                "Invalid rectangle");
}

static jobject
create_rect(JNIEnv *env, pixman_box16_t *box)
{
    return (*env)->NewObject(env, Rect.class, Rect.init, box->x1, box->y1,
            box->x2, box->y2);
}

static jobject
convert_rect(JNIEnv *env, jclass rect, pixman_box16_t *box)
{
    box->x1 = (*env)->GetIntField(env, rect, Rect.left);
    box->y1 = (*env)->GetIntField(env, rect, Rect.top);
    box->x2 = (*env)->GetIntField(env, rect, Rect.right);
    box->y2 = (*env)->GetIntField(env, rect, Rect.bottom);
}

JNIEXPORT jlong JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_create(JNIEnv *env, jclass clazz)
{
    pixman_region16_t *region;

    region = malloc(sizeof *region);
    if (region == NULL) {
        (*env)->ThrowNew(env, OutOfMemoryError, NULL);
        return 0;
    }

    pixman_region_init(region);
    return (jlong)(intptr_t)region;
}

JNIEXPORT jlong JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_create_1rect(JNIEnv *env,
        jclass clazz, jint x, jint y, jint width, jint height)
{
    pixman_region16_t *region;

    region = malloc(sizeof *region);
    if (region == NULL) {
        (*env)->ThrowNew(env, OutOfMemoryError, NULL);
        return 0;
    }

    pixman_region_init_rect(region, x, y, width, height);
    return (jlong)(intptr_t)region;
}

JNIEXPORT jlong JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_clone(JNIEnv *env, jclass clazz,
        jlong reg)
{
    pixman_region16_t *region;

    region = malloc(sizeof *region);
    if (region == NULL) {
        (*env)->ThrowNew(env, OutOfMemoryError, NULL);
        return 0;
    }

    pixman_region_init(region);
    if (! pixman_region_copy(region, (pixman_region16_t *)(intptr_t)reg)) {
        handle_error(env, region);
        pixman_region_fini(region);
        return 0;
    }
    return (jlong)(intptr_t)region;
}

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_destroy(JNIEnv *env, jclass clazz,
        jlong reg)
{
    pixman_region16_t *region = (pixman_region16_t *)(intptr_t)reg;

    pixman_region_fini(region);
    free(region);
}

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_translate(JNIEnv *env, jclass clazz,
        jlong ptr, jint x, jint y)
{
    pixman_region_translate((pixman_region16_t *)(intptr_t)ptr, x, y);
}

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_copy(JNIEnv *env, jclass clazz,
        jlong dest, jlong src)
{
    if (! pixman_region_copy((pixman_region16_t *)(intptr_t)dest,
            (pixman_region16_t *)(intptr_t)src))
        handle_error(env, (pixman_region16_t *)(intptr_t)dest);
}

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_intersect(JNIEnv *env, jclass clazz,
        jlong new_reg, jlong reg1, jlong reg2)
{
    if (! pixman_region_intersect((pixman_region16_t *)(intptr_t)new_reg,
            (pixman_region16_t *)(intptr_t)reg1,
            (pixman_region16_t *)(intptr_t)reg2))
        handle_error(env, (pixman_region16_t *)(intptr_t)new_reg);
}

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_union(JNIEnv *env, jclass clazz,
        jlong new_reg, jlong reg1, jlong reg2)
{
    if (! pixman_region_union((pixman_region16_t *)(intptr_t)new_reg,
            (pixman_region16_t *)(intptr_t)reg1,
            (pixman_region16_t *)(intptr_t)reg2))
        handle_error(env, (pixman_region16_t *)(intptr_t)new_reg);
}

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_union_1rect(JNIEnv *env, jclass clazz,
        jlong dest, jlong source, jint x, jint y, jint width, jint height)
{
    if (! pixman_region_union_rect((pixman_region16_t *)(intptr_t)dest,
            (pixman_region16_t *)(intptr_t)source, x, y, width, height))
        handle_error(env, (pixman_region16_t *)(intptr_t)dest);
}

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_intersect_1rect(JNIEnv *env,
        jclass clazz, jlong dest, jlong source, jint x, jint y,
        jint width, jint height)
{
    if (! pixman_region_intersect_rect((pixman_region16_t *)(intptr_t)dest,
            (pixman_region16_t *)(intptr_t)source, x, y, width, height))
        handle_error(env, (pixman_region16_t *)(intptr_t)dest);
}

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_subtract(JNIEnv *env, jclass clazz,
        jlong reg_d, jlong reg_m, jlong reg_s)
{
    if (! pixman_region_subtract((pixman_region16_t *)(intptr_t)reg_d,
            (pixman_region16_t *)(intptr_t)reg_m,
            (pixman_region16_t *)(intptr_t)reg_s))
        handle_error(env, (pixman_region16_t *)(intptr_t)reg_d); }

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_inverse(JNIEnv *env, jclass clazz,
        jlong new_reg, jlong reg1, jint x1, jint y1, jint x2, jint y2)
{
    pixman_box16_t inv_rect = { x1, y1, x2, y2 };

    if (! pixman_region_inverse((pixman_region16_t *)(intptr_t)new_reg,
            (pixman_region16_t *)(intptr_t)reg1, &inv_rect))
        handle_error(env, (pixman_region16_t *)(intptr_t)new_reg);
}

JNIEXPORT jboolean JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_contains_1point(JNIEnv *env,
        jclass clazz, jlong reg, jint x, jint y)
{
    return pixman_region_contains_point((pixman_region16_t *)(intptr_t)reg,
            x, y, NULL);
}

JNIEXPORT jint JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_n_1rects(JNIEnv *env, jclass clazz,
        jlong reg)
{
    return pixman_region_n_rects((pixman_region16_t *)(intptr_t)reg);
}

JNIEXPORT jobject JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_get_1rect(JNIEnv *env, jclass clazz,
        jlong region, jint idx)
{
    pixman_box16_t *rects;
    int n_rects;

    rects = pixman_region_rectangles((pixman_region16_t *)(intptr_t)region,
            &n_rects);

    if (0 <= idx && idx < n_rects) {
        return create_rect(env, rects + idx);
    } else {
        jni_util_throw_by_name(env, "java/lang/ArrayIndexOutOfBoundsException",
                NULL);
    }
}

JNIEXPORT jboolean JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_equal(JNIEnv *env, jclass clazz,
        jlong reg1, jlong reg2)
{
    return pixman_region_equal((pixman_region16_t *)(intptr_t)reg1,
            (pixman_region16_t *)(intptr_t)reg2);
}

JNIEXPORT void JNICALL
Java_net_jlekstrand_wheatley_PixmanRegion_initializeJNI(JNIEnv *env,
        jclass clazz)
{
    jclass cls;

    cls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
    if (cls == NULL)
        return;
    OutOfMemoryError = (*env)->NewGlobalRef(env, cls);
    (*env)->DeleteLocalRef(env, cls);
    if (OutOfMemoryError == NULL)
        return;

    cls = (*env)->FindClass(env, "net/jlekstrand/wheatley/Rect");
    if (cls == NULL)
        return;
    Rect.class = (*env)->NewGlobalRef(env, cls);
    (*env)->DeleteLocalRef(env, cls);
    if (Rect.class == NULL)
        return;
    Rect.init = (*env)->GetMethodID(env, Rect.class, "<init>", "(IIII)V");
    if (Rect.init == NULL)
        return;
    Rect.left = (*env)->GetFieldID(env, Rect.class, "left", "I");
    if (Rect.left == NULL)
        return;
    Rect.top = (*env)->GetFieldID(env, Rect.class, "top", "I");
    if (Rect.top == NULL)
        return;
    Rect.right = (*env)->GetFieldID(env, Rect.class, "right", "I");
    if (Rect.right == NULL)
        return;
    Rect.bottom = (*env)->GetFieldID(env, Rect.class, "bottom", "I");
    if (Rect.bottom == NULL)
        return;
}

