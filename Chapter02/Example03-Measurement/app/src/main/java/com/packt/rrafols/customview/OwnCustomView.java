package com.packt.rrafols.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class OwnCustomView extends View {
    private static final String TAG = OwnCustomView.class.getName();
    private Paint backgroundPaint;
    private static final int DEFAULT_SIZE = 2000;

    public OwnCustomView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xffff0000);
        backgroundPaint.setStyle(Paint.Style.FILL);

    }

    private static int getMeasurementSize(int measureSpec, int defaultSize) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        switch(mode) {
            case MeasureSpec.EXACTLY:
                return size;

            case MeasureSpec.AT_MOST:
                return Math.min(defaultSize, size);

            case MeasureSpec.UNSPECIFIED:
            default:
                return defaultSize;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getMeasurementSize(widthMeasureSpec, DEFAULT_SIZE);
        int height = getMeasurementSize(heightMeasureSpec, DEFAULT_SIZE);
        setMeasuredDimension(width, height);
    }

    public final int dpToPixels(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5);
    }

    public final int pixelsToDp(int dp) {
        return (int) (dp / getResources().getDisplayMetrics().density + 0.5);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        super.onDraw(canvas);
    }
}