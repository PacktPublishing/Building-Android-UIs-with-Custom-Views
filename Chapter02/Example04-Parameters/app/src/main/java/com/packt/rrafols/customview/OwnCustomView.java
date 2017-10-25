package com.packt.rrafols.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class OwnCustomView extends View {
    private static final String TAG = OwnCustomView.class.getName();
    private static final int DEFAULT_SIZE = 2000;
    private static final int DEFAULT_FILL_COLOR = 0xffff0000;
    private Paint backgroundPaint;


    public OwnCustomView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        int fillColor;

        TypedArray ta = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.OwnCustomView, 0, 0);
        try {
            fillColor = ta.getColor(R.styleable.OwnCustomView_fillColor, DEFAULT_FILL_COLOR);
        } finally {
            ta.recycle();
        }

        backgroundPaint = new Paint();
        backgroundPaint.setColor(fillColor);
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