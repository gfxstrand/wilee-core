package net.jlekstrand.wayland;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import net.jlekstrand.wayland.compositor.Compositor;
import net.jlekstrand.wayland.compositor.Renderer;
import net.jlekstrand.wayland.compositor.GLES20Renderer;

public class WaylandActivity extends Activity
{
    SurfaceView sView;
    Renderer renderer;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        sView = new SurfaceView(this);
        SurfaceHolder holder = sView.getHolder();
        renderer = new GLES20Renderer();
        holder.addCallback(renderer);
        setContentView(sView);

        /*
        try {
            Thread.sleep(7 * 1000);
        } catch (InterruptedException e) {
        }
        */

        /*

        final Compositor comp = new Compositor();
        new Thread(new Runnable() {
            public void run()
            {
                comp.run();
            }
        }).start();
        */
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
    }

    @Override
    protected void
    onPause()
    {
        super.onPause();
    }

    @Override
    protected void
    onStop()
    {
        super.onStop();
    }
}
