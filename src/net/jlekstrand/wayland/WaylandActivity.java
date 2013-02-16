package net.jlekstrand.wayland;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.util.Log;

import net.jlekstrand.wayland.compositor.Compositor;
import net.jlekstrand.wayland.compositor.GLES20Renderer;

public class WaylandActivity extends Activity implements SurfaceHolder.Callback
{
    SurfaceView sView;
    GLES20Renderer renderer;
    Thread renderThread;

    Compositor compositor;
    boolean has_surface;
    boolean running;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        sView = new SurfaceView(this);
        SurfaceHolder holder = sView.getHolder();
        holder.addCallback(this);
        setContentView(sView);

        /*
        try {
            Thread.sleep(7 * 1000);
        } catch (InterruptedException e) {
        }
        */

        has_surface = false;
        running = false;

        renderer = new GLES20Renderer(this);
        compositor = new Compositor();
        new Thread(new Runnable() {
            public void run()
            {
                compositor.run();
            }
        }).start();
    }

    @Override
    protected void
    onStart()
    {
        super.onStart();
    }

    @Override
    protected void
    onResume()
    {
        super.onResume();
        running = true;
        if (has_surface)
            compositor.setRenderer(renderer);
    }

    @Override
    protected void
    onPause()
    {
        super.onPause();
        running = false;
        compositor.setRenderer(null);
    }

    @Override
    protected void
    onStop()
    {
        super.onStop();
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder holder)
    {
        has_surface = false;
        compositor.setRenderer(null);
        renderer.surfaceDestroyed(holder);
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder,
            int format, int width, int height)
    {
        renderer.surfaceChanged(holder, format, width, height);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder)
    {
        renderer.surfaceCreated(holder);
        has_surface = true;
        if (running)
            compositor.setRenderer(renderer);
    }
}
