package com.packt.rrafols.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CustomLayout customLayout = (CustomLayout) findViewById(R.id.custom_layout);

        Random rnd = new Random();
        for(int i = 0; i < 50; i++) {
            OwnCustomView view = new OwnCustomView(this);

            int width = rnd.nextInt(200) + 50;
            int height = rnd.nextInt(100) + 100;
            view.setLayoutParams(new ViewGroup.LayoutParams(width, height));
            view.setPadding(2, 2, 2, 2);

            customLayout.addView(view);
        }
    }
}
