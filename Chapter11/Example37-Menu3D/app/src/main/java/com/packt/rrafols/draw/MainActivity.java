package com.packt.rrafols.draw;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        GLDrawer glDrawer = (GLDrawer) findViewById(R.id.gldrawer);
        glDrawer.setOnMenuClickedListener(new GLDrawer.OnMenuClickedListener() {
            @Override
            public void menuClicked(int option) {
                Log.i("Example37-Menu3D", "option clicked " + (option + 1)) ;
            }
        });
        glDrawer.setColors(new int[] {
                0xff4a90e2,
                0xff161616,
                0xff594236,
                0xffff5964,
                0xff8aea92,
                0xffffe74c
        });

        glDrawer.setNumOptions(6);
    }
}
