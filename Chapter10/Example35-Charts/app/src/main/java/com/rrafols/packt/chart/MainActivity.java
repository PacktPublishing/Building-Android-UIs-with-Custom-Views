package com.rrafols.packt.chart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Chart chart = (Chart) findViewById(R.id.chart_view);

        float[] data = new float[15];
        for(int i = 0; i < data.length; i++) {
            data[i] = (float) Math.random() * 10.f;
        }
        chart.setDataPoints(data, 0);

        for(int i = 0; i < data.length; i++) {
            data[i] = (float) Math.random() * 10.f;
        }
        chart.setDataPoints(data, 1);
    }
}
