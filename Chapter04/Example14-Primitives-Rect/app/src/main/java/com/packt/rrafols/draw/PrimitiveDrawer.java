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

    private static final int BACKGROUND_COLOR = 0xffffa020;

    private Paint paint;
    private ArrayList<Float> rects;
    private ArrayList<Integer> colors;

    public PrimitiveDrawer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        rects = new ArrayList<>();
        colors = new ArrayList<>();

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(BACKGROUND_COLOR);

        int width = getWidth();
        int height = getHeight();

        for (int i = 0; i < 2; i++) {
            rects.add((float) Math.random() * width);
            rects.add((float) Math.random() * height);
        }
        colors.add(0xff000000 | (int) (0xffffff * Math.random()));

        for (int i = 0; i < rects.size() / 4; i++) {
            paint.setColor(colors.get(i));
            canvas.drawRoundRect(
                    rects.get(i * 4    ),
                    rects.get(i * 4 + 1),
                    rects.get(i * 4 + 2),
                    rects.get(i * 4 + 3),
                    40, 40, paint);
        }

        if (rects.size() < 400) postInvalidateDelayed(20);
    }
}