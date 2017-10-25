package com.packt.rrafols.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
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
    private int topLeftColor = DEFAULT_FILL_COLOR;
    private int bottomLeftColor = DEFAULT_FILL_COLOR;
    private int topRightColor = DEFAULT_FILL_COLOR;
    private int bottomRightColor = DEFAULT_FILL_COLOR;
    private boolean needsUpdate = false;

    public OwnCustomView(Context context) {
        super(context);

        init();
    }

    public OwnCustomView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
    }

    public void setTopLeftColor(int topLeftColor) {
        this.topLeftColor = topLeftColor;
        needsUpdate = true;
    }

    public void setBottomLeftColor(int bottomLeftColor) {
        this.bottomLeftColor = bottomLeftColor;
        needsUpdate = true;
    }

    public void setTopRightColor(int topRightColor) {
        this.topRightColor = topRightColor;
        needsUpdate = true;
    }

    public void setBottomRightColor(int bottomRightColor) {
        this.bottomRightColor = bottomRightColor;
        needsUpdate = true;
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

    @Override
    protected void onDraw(Canvas canvas) {
        if (needsUpdate) {
            int[] colors = new int[] {topLeftColor, topRightColor, bottomRightColor, bottomLeftColor};

            LinearGradient lg = new LinearGradient(0, 0, getWidth(), getHeight(), colors, null, Shader.TileMode.CLAMP);
            backgroundPaint.setShader(lg);
            needsUpdate = false;
        }

        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        super.onDraw(canvas);
    }
}