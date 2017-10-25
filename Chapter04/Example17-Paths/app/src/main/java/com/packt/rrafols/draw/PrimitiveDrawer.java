package com.packt.rrafols.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class PrimitiveDrawer extends View {
    private static final String TAG = PrimitiveDrawer.class.getName();

    private static final int POINTS = 20;
    private static final int BACKGROUND_COLOR = 0xff000040;

    private Paint paint;
    private Path path;
    private Bitmap background;
    private Matrix backgroundTranformation;
    private boolean touching;

    public PrimitiveDrawer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(0xffff0000);

        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        backgroundTranformation = new Matrix();
        touching = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (path == null) {
            float[] points = new float[POINTS * 3];
            for(int i = 0; i < POINTS; i++) {
                points[i * 3    ] = (float) Math.random() * getWidth();
                points[i * 3 + 1] = (float) Math.random() * getHeight();
                points[i * 3 + 2] = (float) Math.random() * (getWidth()/4);
            }

            path = new Path();

            for (int i = 0; i < points.length / 3; i++) {
                path.addCircle(
                        points[i * 3    ],
                        points[i * 3 + 1],
                        points[i * 3 + 2],
                        Path.Direction.CW);
            }

            path.close();
        }

        canvas.save();

        if(!touching) canvas.clipPath(path);
        if(background != null) {
            backgroundTranformation.reset();
            float scale = ((float) getWidth()) / background.getWidth();
            backgroundTranformation.postScale(scale, scale);
            canvas.drawBitmap(background, backgroundTranformation, null);
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touching = true;
            postInvalidate();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP  ) {
            touching = false;
            postInvalidate();
            return true;

        }

        return false;
    }
}