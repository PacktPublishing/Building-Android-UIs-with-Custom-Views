package com.packt.rrafols.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    private static final int BRIGHT_GREEN = 0xff00ff00;
    private static final int BRIGHT_RED = 0xffff0000;
    private static final int BRIGHT_YELLOW = 0xffffff00;
    private static final int BRIGHT_BLUE = 0xff0000ff;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        OwnCustomView customView = new OwnCustomView.Builder(this)
                .topLeftColor(BRIGHT_RED)
                .topRightColor(BRIGHT_GREEN)
                .bottomLeftColor(BRIGHT_YELLOW)
                .bottomRightColor(BRIGHT_BLUE)
                .build();

        linearLayout.addView(customView);

        setContentView(linearLayout);
    }
}
