package com.packt.rrafols.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

public class PerformanceExample extends View {
    private static final String TAG = PerformanceExample.class.getName();

    private static final int BLACK_COLOR = 0xff000000;
    private static final int WHITE_COLOR = 0xffffffff;
    private float angle;

    public PerformanceExample(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        angle = 0.f;
    }

    /**
     * This is precisely an example of what MUST be avoided.
     * It is just to exemplify chapter 7.
     *
     * DO NOT USE.
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Rect rect = new Rect(0, 0, getWidth(), getHeight());
        Paint paint = new Paint();
        paint.setColor(BLACK_COLOR);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rect, paint);

        canvas.save();

        canvas.rotate(angle, getWidth() / 2, getHeight() / 2);
        canvas.translate((getWidth() - getWidth()/4) / 2,
                         (getHeight() - getHeight()/4) / 2);

        rect = new Rect(0, 0, getWidth() / 4, getHeight() / 4);
        paint = new Paint();
        paint.setColor(WHITE_COLOR);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.drawRect(rect, paint);

        canvas.restore();
        invalidate();



        bitmap.recycle();
        angle += 0.1f;
    }
}