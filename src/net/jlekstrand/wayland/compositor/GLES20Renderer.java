package net.jlekstrand.wayland.compositor;

import java.nio.FloatBuffer;

import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.opengl.GLES20;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;


public class GLES20Renderer extends AbstractRenderer
{
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private static final int EGL_OPENGL_ES2_BIT = 0x0004;

    public class ShaderCompileError extends Error
    {
        public ShaderCompileError(String message)
        {
            super(message);
        }

        public ShaderCompileError(int shader)
        {
            super(GLES20.glGetShaderInfoLog(shader));
        }
    }
    
    public class ProgramLinkError extends Error
    {
        public ProgramLinkError(String message)
        {
            super(message);
        }

        public ProgramLinkError(int program)
        {
            super(GLES20.glGetProgramInfoLog(program));
        }
    }

    QueuedExecutorThread renderThread;

    EGLDisplay display;
    EGLConfig config;
    EGLSurface surface;
    EGLContext context;
    EGL10 egl;

    public GLES20Renderer()
    { }

    private EGLConfig chooseEGLConfig()
    {
        int config_attribs[] = {
            EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] num_configs = new int[1];

        egl.eglChooseConfig(display, config_attribs, configs, 1,
                num_configs);
        
        if (num_configs[0] < 1)
            return null;
        else
            return configs[0];
    }

    @Override
    protected void onRender(Shell shell)
    {
        egl.eglMakeCurrent(display, surface, surface, context);
        Log.d("wayland:Renderer", "Rendering...");
        GLES20.glClearColor(0, 0, 1, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        egl.eglSwapBuffers(display, surface);
    }

    @Override
    protected void onSurfaceCreated(SurfaceHolder holder)
    {
        Log.d("wayland:Renderer", "Initializing Native Surface");
        egl = (EGL10)EGLContext.getEGL();

        display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        egl.eglInitialize(display, null);

        config = chooseEGLConfig();
        if (config == null) {
            Log.d("wayland:Renderer",
                    "Failed to find suitable EGL Config");
            return;
        }

        surface = egl.eglCreateWindowSurface(display, config,
                holder.getSurface(), null);
        if (surface == EGL10.EGL_NO_SURFACE) {
            Log.d("wayland:Renderer",
                    "Failed to create EGL Surface");
            return;
        }

        int context_attribs[] = {
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL10.EGL_NONE
        };

        context = egl.eglCreateContext(display, config,
                EGL10.EGL_NO_CONTEXT, context_attribs);
        if (context == EGL10.EGL_NO_CONTEXT) {
            Log.d("wayland:Renderer", "Failed to create EGL Context");
            return;
        }
        egl.eglMakeCurrent(display, surface, surface, context);
    }

    @Override
    protected void onSurfaceChanged(SurfaceHolder holder, int format,
            final int width, final int height)
    {
        egl.eglMakeCurrent(display, surface, surface, context);
        Log.d("wayland:Renderer", "Resetting Viewport: " + width + "x" + height);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(0, 0, 1, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT
                | GLES20.GL_DEPTH_BUFFER_BIT);

        egl.eglSwapBuffers(display, surface);
    }

    @Override
    protected void onSurfaceDestroyed(SurfaceHolder holder)
    {
        egl.eglDestroyContext(display, context);
        egl.eglDestroySurface(display, surface);
        return;
    }

    private int createAndCompileShader(int shaderType, String source)
    {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader == 0)
            throw new OutOfMemoryError("Failed to create shader");

        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);

        int status[] = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            GLES20.glDeleteShader(shader);
            throw new ShaderCompileError(shader);
        }

        return shader;
    }
}

