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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(BACKGROUND_COLOR);

        if (points == null) {
            points = new float[POINTS * 2];
            for(int i = 0; i < POINTS; i++) {
                points[i * 2    ] = (float) Math.random() * getWidth();
                points[i * 2 + 1] = (float) Math.random() * getHeight();
            }
        }

        paint.setColor(0xffa0a0a0);
        paint.setStrokeWidth(4.f);
        paint.setStrokeCap(Paint.Cap.BUTT);
        canvas.drawLines(points, paint);

        paint.setColor(0xffffffff);
        paint.setStrokeWidth(10.f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawPoints(points, paint);
    }
}