package net.jlekstrand.wayland;

import android.app.Activity;
import android.os.Bundle;

public class WaylandActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /*
        try {
            Thread.sleep(7 * 1000);
        } catch (InterruptedException e) {
        }
        */

        final Compositor comp = new Compositor();

        new Thread(new Runnable() {
            public void run()
            {
                comp.run();
            }
        }).start();
    }
}
