package net.jlekstrand.wayland.compositor;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.io.IOException;
import java.util.HashMap;

import android.util.Log;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Region;
import android.view.SurfaceHolder;
import android.opengl.GLES20;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import org.freedesktop.wayland.server.Listener;
import org.freedesktop.wayland.protocol.wl_shm;

public class GLES20Renderer extends AbstractSurfaceRenderer
{
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private static final int EGL_OPENGL_ES2_BIT = 0x0004;

    private static final String LOG_PREFIX = "wayland:GLES20Renderer";

    public class ShaderCompileError extends Error
    {
        public ShaderCompileError(String message)
        {
            super(message);
        }

        public ShaderCompileError(String filename, int shader)
        {
            super(filename + ": " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
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
            GLES20.glDeleteProgram(program);
        }
    }

    private enum ShaderProgram {
        SHM_ARGB8888("surface.vert", "surface_argb8888.frag"),
        SHM_XRGB8888("surface.vert", "surface_xrgb8888.frag");

        public String vertexShaderAsset;
        public String fragmentShaderAsset;

        ShaderProgram(String vAsset, String fAsset)
        {
            this.vertexShaderAsset = vAsset;
            this.fragmentShaderAsset = fAsset;
        }
    }

    private class SurfaceData
    {
        private Surface surface;

        public int texture;
        public int tex_width;
        public int tex_height;
        public ShaderProgram program;

        public SurfaceData(Surface surface)
        {
            this.surface = surface;

            int textures[] = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            // Set up texture parameters
            GLES20.glBindTexture(texture, GLES20.GL_TEXTURE_2D);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);

            surface.addDestroyListener(new Listener() {
                @Override
                public void onNotify()
                {
                    destroy();
                    surfaceDataCache.remove(this);
                }
            });
        }

        public void refresh()
        {
            GLES20.glBindTexture(texture, GLES20.GL_TEXTURE_2D);
            Buffer buffer = surface.getBuffer();
            Region damage = surface.getDamage();

            if (buffer instanceof ShmBuffer) {
                ShmBuffer shmBuffer = (ShmBuffer)buffer;
                ByteBuffer bufferData = shmBuffer.getBuffer();

                switch(shmBuffer.getFormat()) {
                case wl_shm.FORMAT_ARGB8888:
                    program = ShaderProgram.SHM_ARGB8888; 
                    break;
                default:
                case wl_shm.FORMAT_XRGB8888:
                    program = ShaderProgram.SHM_XRGB8888; 
                    break;
                }

                tex_width = shmBuffer.getStride() / 4;
                tex_height = shmBuffer.getHeight();

                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                        tex_width, tex_height, 0, GLES20.GL_RGBA,
                        GLES20.GL_UNSIGNED_BYTE, bufferData);
            }
        }

        public void destroy()
        {
            GLES20.glDeleteTextures(1, new int[]{texture}, 0);
        }
    }

    private final AssetManager assetManager;

    private final HashMap<String, Integer> shaderCache;
    private final HashMap<ShaderProgram, Integer> shaderPrograms;
    private final HashMap<Surface, SurfaceData> surfaceDataCache;

    private float projectionMatrix[];

    private EGLDisplay eglDisplay;
    private EGLSurface eglSurface;
    private EGLContext eglContext;
    private EGL10 egl;

    public GLES20Renderer(Context context)
    {
        this.assetManager = context.getAssets();

        shaderCache = new HashMap<String, Integer>();
        shaderPrograms = new HashMap<ShaderProgram, Integer>();
        surfaceDataCache = new HashMap<Surface, SurfaceData>();

        projectionMatrix = new float[16];
    }

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

        egl.eglChooseConfig(eglDisplay, config_attribs, configs, 1,
                num_configs);
        
        if (num_configs[0] < 1)
            return null;
        else
            return configs[0];
    }

    private int getShader(String assetName)
    {
        Integer cachedShader = shaderCache.get(assetName);
        if (cachedShader != null)
            return cachedShader;

        final int shaderType;
        if (assetName.toLowerCase().endsWith(".frag")) {
            shaderType = GLES20.GL_FRAGMENT_SHADER;
        } else if (assetName.toLowerCase().endsWith(".vert")) {
            shaderType = GLES20.GL_VERTEX_SHADER;
        } else {
            throw new IllegalArgumentException(
                    "Invalid shader source file name: " + assetName);
        }

        /* Otherwise, we need to load this new shader */

        final String source;
        try {
            java.io.InputStream stream = assetManager.open("shaders/" + assetName);
            // Got this trick from StackOverflow:
            // http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
            source = (new java.util.Scanner(stream).useDelimiter("\\A")).next();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read shader source", e);
        }

        int shader = GLES20.glCreateShader(shaderType);
        if (shader == 0)
            throw new OutOfMemoryError("Failed to create shader");

        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);

        int status[] = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            throw new ShaderCompileError(assetName, shader);
        }

        shaderCache.put(assetName, shader);

        return shader;
    }

    private int createProgram(ShaderProgram program)
    {
        int vshader = getShader(program.vertexShaderAsset);
        int fshader = getShader(program.fragmentShaderAsset);

        int progName = GLES20.glCreateProgram();
        GLES20.glAttachShader(progName, vshader);
        GLES20.glAttachShader(progName, fshader);
        GLES20.glLinkProgram(progName);

        int status[] = new int[1];
        GLES20.glGetProgramiv(progName, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            GLES20.glDeleteShader(vshader);
            GLES20.glDeleteShader(fshader);
            throw new ProgramLinkError(progName);
        }

        return progName;
    }

    private void useProgram(ShaderProgram program)
    {
        Integer progName = shaderPrograms.get(program);

        if (progName == null) {
            progName = createProgram(program);
            shaderPrograms.put(program, progName);
        }

        GLES20.glUseProgram(progName);
    }

    private void createShaderPrograms()
    {
        createProgram(ShaderProgram.SHM_ARGB8888);
        createProgram(ShaderProgram.SHM_XRGB8888);
    }

    @Override
    protected void onBeginRender(boolean clear)
    {
        egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);

        if (clear) {
            GLES20.glClearColor(0, 0, 1, 1);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
    }

    @Override
    protected int onEndRender()
    {
        egl.eglSwapBuffers(eglDisplay, eglSurface);
        return super.onEndRender();
    }

    @Override
    protected void onDrawSurface(Surface surface)
    {
        SurfaceData surfaceData = surfaceDataCache.get(surface);
        if (surfaceData == null) {
            surfaceData = new SurfaceData(surface);
            surfaceDataCache.put(surface, surfaceData);
        }

        surfaceData.refresh();

        useProgram(surfaceData.program);

        int progName = shaderPrograms.get(surfaceData.program);

        int uniform = GLES20.glGetUniformLocation(progName, "vu_texture_size");
        GLES20.glUniform2f(uniform, surfaceData.tex_width,
                surfaceData.tex_height);
        uniform = GLES20.glGetUniformLocation(progName, "vu_projection");
        GLES20.glUniformMatrix4fv(uniform, 1, false, projectionMatrix, 0);
        uniform = GLES20.glGetUniformLocation(progName, "vu_transformation");
        GLES20.glUniformMatrix4fv(uniform, 1, false, new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        }, 0);

        uniform = GLES20.glGetUniformLocation(progName, "fu_texture");
        GLES20.glUniform1i(uniform, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, surfaceData.texture);

        int width = surface.getBuffer().getWidth();
        int height = surface.getBuffer().getHeight();

        ByteBuffer vertices = ByteBuffer.allocateDirect(12 * 4);
        vertices.order(ByteOrder.nativeOrder());
        vertices.asFloatBuffer().put(new float[]{
            0, 0,
            0, height,
            width, 0,
            0, height,
            width, 0,
            width, height
        });

        int attrib = GLES20.glGetAttribLocation(progName, "va_vertex");
        GLES20.glEnableVertexAttribArray(attrib);
        GLES20.glVertexAttribPointer(attrib, 2, GLES20.GL_FLOAT, false, 0,
                vertices);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }

    @Override
    protected void onSurfaceCreated(SurfaceHolder holder)
    {
        Log.d(LOG_PREFIX, "Initializing Native Surface");
        egl = (EGL10)EGLContext.getEGL();

        eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        egl.eglInitialize(eglDisplay, null);

        EGLConfig config = chooseEGLConfig();
        if (config == null) {
            Log.d(LOG_PREFIX, "Failed to find suitable EGL Config");
            return;
        }

        eglSurface = egl.eglCreateWindowSurface(eglDisplay, config,
                holder.getSurface(), null);
        if (eglSurface == EGL10.EGL_NO_SURFACE) {
            Log.d(LOG_PREFIX, "Failed to create EGL Surface");
            return;
        }

        int context_attribs[] = {
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL10.EGL_NONE
        };

        eglContext = egl.eglCreateContext(eglDisplay, config,
                EGL10.EGL_NO_CONTEXT, context_attribs);
        if (eglContext == EGL10.EGL_NO_CONTEXT) {
            Log.d(LOG_PREFIX, "Failed to create EGL Context");
            return;
        }
        egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);

        createShaderPrograms();

        egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
        GLES20.glClearColor(0, 1, 0, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        egl.eglSwapBuffers(eglDisplay, eglSurface);
    }

    @Override
    protected void onSurfaceChanged(SurfaceHolder holder, int format,
            final int width, final int height)
    {
        egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
        Log.d(LOG_PREFIX, "Resetting Viewport: " + width + "x" + height);
        GLES20.glViewport(0, 0, width, height);

        Matrix.orthoM(projectionMatrix, 0, 0, width, height, 0, -1, 1);
    }

    @Override
    protected void onSurfaceDestroyed(SurfaceHolder holder)
    {
        for (Integer progName : shaderPrograms.values())
            GLES20.glDeleteProgram(progName);
        shaderPrograms.clear();

        for (Integer shaderName : shaderCache.values())
            GLES20.glDeleteShader(shaderName);
        shaderCache.clear();

        for (SurfaceData data : surfaceDataCache.values())
            data.destroy();
        surfaceDataCache.clear();

        egl.eglDestroyContext(eglDisplay, eglContext);
        egl.eglDestroySurface(eglDisplay, eglSurface);
        return;
    }
}

