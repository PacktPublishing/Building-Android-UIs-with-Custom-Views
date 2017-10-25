package com.packt.rrafols.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    private static final int BRIGHT_GREEN = 0xff00ff00;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        OwnCustomView customView = new OwnCustomView(this);
        customView.setFillColor(BRIGHT_GREEN);
        linearLayout.addView(customView);

        setContentView(linearLayout);
    }
}
