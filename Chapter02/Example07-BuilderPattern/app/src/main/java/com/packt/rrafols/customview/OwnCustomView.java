package com.packt.rrafols.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class OwnCustomView extends View {
    private static final String TAG = OwnCustomView.class.getName();
    private static final int DEFAULT_SIZE = 2000;
    private static final int DEFAULT_FILL_COLOR = 0xffff0000;
    private static final int PREVIEW_RED = 0xffff0000;
    private static final int PREVIEW_BLUE = 0xff0000ff;
    private static final int PREVIEW_GREEN = 0xff00ff00;
    private static final int PREVIEW_BLACK = 0xff000000;
    private Paint backgroundPaint;
    private boolean firstDraw;
    private int[] colorArray;

    private OwnCustomView(Builder builder) {
        super(builder.context);

        init(builder.topLeftColor,
             builder.topRightColor,
             builder.bottomLeftColor,
             builder.bottomRightColor);
    }

    public OwnCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            init(PREVIEW_RED,
                 PREVIEW_BLUE,
                 PREVIEW_GREEN,
                 PREVIEW_BLACK);

        } else {
            throw new IllegalStateException("Not supposed to be parametrized from XML in this example");
        }
    }

    private void init(int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor) {
        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);

        colorArray = new int[] {
                topLeftColor,
                topRightColor,
                bottomRightColor,
                bottomLeftColor
        };

        firstDraw = true;
    }

    public static class Builder {
        private Context context;
        private int topLeftColor = DEFAULT_FILL_COLOR;
        private int topRightColor = DEFAULT_FILL_COLOR;
        private int bottomLeftColor = DEFAULT_FILL_COLOR;
        private int bottomRightColor = DEFAULT_FILL_COLOR;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder topLeftColor(int topLeftColor) {
            this.topLeftColor = topLeftColor;
            return this;
        }

        public Builder topRightColor(int topRightColor) {
            this.topRightColor = topRightColor;
            return this;
        }

        public Builder bottomLeftColor(int bottomLeftColor) {
            this.bottomLeftColor = bottomLeftColor;
            return this;
        }

        public Builder bottomRightColor(int bottomRightColor) {
            this.bottomRightColor = bottomRightColor;
            return this;
        }

        public OwnCustomView build() {
            return new OwnCustomView(this);
        }
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
        if (firstDraw) {
            LinearGradient lg = new LinearGradient(0, 0, getWidth(), getHeight(),
                    colorArray, null, Shader.TileMode.CLAMP);

            backgroundPaint.setShader(lg);
            firstDraw = false;
        }

        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        super.onDraw(canvas);
    }
}