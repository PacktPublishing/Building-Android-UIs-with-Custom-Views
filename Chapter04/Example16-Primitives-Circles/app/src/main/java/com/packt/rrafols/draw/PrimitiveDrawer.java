package com.packt.rrafols.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class PrimitiveDrawer extends View {
    private static final String TAG = PrimitiveDrawer.class.getName();

    private static final int POINTS = 20;
    private static final int BACKGROUND_COLOR = 0xff000040;

    private Paint paint;
    private float[] points;

    public PrimitiveDrawer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(0xffffffff);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(BACKGROUND_COLOR);

        if (points == null) {
            points = new float[POINTS * 3];
            for(int i = 0; i < POINTS; i++) {
                points[i * 3    ] = (float) Math.random() * getWidth();
                points[i * 3 + 1] = (float) Math.random() * getHeight();
                points[i * 3 + 2] = (float) Math.random() * (getWidth()/4);
            }
        }

        for (int i = 0; i < points.length / 3; i++) {
            canvas.drawCircle(
                    points[i * 3    ],
                    points[i * 3 + 1],
                    points[i * 3 + 2],
                    paint);
        }
    }
}