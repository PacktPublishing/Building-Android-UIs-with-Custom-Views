package com.rrafols.packt.chart;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Chart extends View {
    private static final String TAG = Chart.class.getName();
    private static final int TIME_THRESHOLD = 16;
    private static final float ANIM_THRESHOLD = 0.01f;

    private float scrollX;
    private float scrollY;
    private float scrollXTarget;
    private float scrollYTarget;
    private float frScrollX;
    private float frScrollY;

    private long timeStart;
    private long accTime;
    private ArrayList<Float>[] dataPoints;
    private Paint backgroundPaint;
    private Paint[] linePaint;
    private Paint[] circlePaint;
    private Path[] graphPath;
    private Path[] circlePath;
    private Path backgroundPath;
    private boolean regenerate;
    private float lastWidth;
    private float lastHeight;
    private Rect textBoundaries;
    private float minValue;
    private float maxValue;
    private float verticalDelta;
    private DecimalFormat decimalFormat;
    private String[] labels;
    private String[] verticalLabels;
    private boolean invertVerticalAxis;
    private boolean generateLabels;
    private boolean drawLegend;
    private ScaleGestureDetector scaleDetector;
    private float scale;
    private float dragX;
    private float dragY;
    private boolean zooming;
    private boolean dragged;
    private float maxLabelWidth;

    public Chart(Context context, AttributeSet attrs) {
        super(context, attrs);

        linePaint = new Paint[2];
        linePaint[0] = new Paint();
        linePaint[0].setAntiAlias(true);
        linePaint[0].setColor(0xffffffff);
        linePaint[0].setStrokeWidth(8.f);
        linePaint[0].setStyle(Paint.Style.STROKE);

        linePaint[1] = new Paint(linePaint[0]);
        linePaint[1].setColor(0xff4040ff);


        circlePaint = new Paint[2];
        circlePaint[0] = new Paint();
        circlePaint[0].setAntiAlias(true);
        circlePaint[0].setColor(0xffff2020);
        circlePaint[0].setStyle(Paint.Style.FILL);

        circlePaint[1] = new Paint(circlePaint[0]);
        circlePaint[1].setColor(0xff20ff20);


        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xffFFFF80);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setPathEffect(new DashPathEffect(new float[] {5, 5}, 0));
        backgroundPaint.setTextSize(20.f);

        graphPath = new Path[2];
        graphPath[0] = new Path();
        graphPath[1] = new Path();

        circlePath = new Path[2];
        circlePath[0] = new Path();
        circlePath[1] = new Path();

        backgroundPath = new Path();
        lastWidth = -1;
        lastHeight = -1;

        textBoundaries = new Rect();
        decimalFormat = new DecimalFormat("#.##");
        verticalLabels = new String[11];

        invertVerticalAxis = false;
        drawLegend = true;
        generateLabels = true;
        dataPoints = (ArrayList<Float>[]) new ArrayList[2];
        zooming = false;
        scale = 1.f;
        maxLabelWidth = 0.f;

        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private float focusX;
            private float focusY;
            private float scrollCorrectionX = 0.f;
            private float scrollCorrectionY = 0.f;
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                zooming = true;
                focusX = detector.getFocusX();
                focusY = detector.getFocusY();
                scrollCorrectionX = focusX * scale - scrollXTarget;
                scrollCorrectionY = focusY * scale - scrollYTarget;
                return true;
            }

            public boolean onScale(ScaleGestureDetector detector) {
                scale *= detector.getScaleFactor();
                scale = Math.max(1.f, Math.min(scale, 2.f));

                float currentX = focusX * scale - scrollXTarget;
                float currentY = focusY * scale - scrollYTarget;

                scrollXTarget += currentX - scrollCorrectionX;
                scrollYTarget += currentY - scrollCorrectionY;

                invalidate();
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                zooming = true;
            }
        });

        timeStart = SystemClock.elapsedRealtime();
    }

    public void setDataPoints(float[] originalData) {
        setDataPoints(originalData, 0);
    }

    public void setDataPoints(float[] originalData, int index) {
        ArrayList<Float> array = new ArrayList<>();
        for (float data : originalData) {
            array.add(data);
        }

        setDataPoints(array, index);
    }

    public void setDataPoints(ArrayList<Float> originalData) {
        setDataPoints(originalData, 0);
    }

    public void setDataPoints(ArrayList<Float> originalData, int index) {
        dataPoints[index] = new ArrayList<Float>();
        dataPoints[index].addAll(originalData);

        adjustDataRange();
    }

    private void adjustDataRange() {
        minValue = Float.MAX_VALUE;
        maxValue = Float.MIN_VALUE;
        for (int j = 0; j < dataPoints.length; j++) {
            for (int i = 0; dataPoints[j] != null && i < dataPoints[j].size(); i++) {
                if (dataPoints[j].get(i) < minValue) minValue = dataPoints[j].get(i);
                if (dataPoints[j].get(i) > maxValue) maxValue = dataPoints[j].get(i);
            }
        }

        verticalDelta = maxValue - minValue;

        regenerate = true;
        postInvalidate();
    }

    public void addValue(float data) {
        addValue(data, 0);
    }

    public void addValue(float data, int index) {
        dataPoints[index].add(data);

        if (data < minValue || data > maxValue) {
            adjustDataRange();
        } else {
            regenerate = true;
            postInvalidate();
        }
    }

    public void setInvertVerticalAxis(boolean invertVerticalAxis) {
        this.invertVerticalAxis = invertVerticalAxis;
        regenerate = true;
        postInvalidate();
    }

    public void setDrawLegend(boolean drawLegend) {
        this.drawLegend = drawLegend;
        regenerate = true;
        postInvalidate();
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
        generateLabels = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        animateLogic();

        canvas.drawARGB(255,0 ,0 ,0);
        canvas.save();

        canvas.translate(-frScrollX, -frScrollY);
        canvas.scale(scale, scale);

        if (drawLegend && regenerate) {
            for (int i = 0; i <= 10; i++) {
                float step;

                if (!invertVerticalAxis) {
                    step = ((float) i / 10.f);
                } else {
                    step = ((float) (10 - i)) / 10.f;
                }

                float value = step * verticalDelta + minValue;
                verticalLabels[i] = decimalFormat.format(value);
                backgroundPaint.getTextBounds(verticalLabels[i], 0, verticalLabels[i].length(), textBoundaries);
                if (textBoundaries.width() > maxLabelWidth) {
                    maxLabelWidth = textBoundaries.width();
                }
            }
        }

        float labelLeftPadding = getPaddingLeft() + maxLabelWidth * 0.25f;
        float leftPadding = getPaddingLeft() + maxLabelWidth * 1.5f;
        float rightPadding = getPaddingRight();
        float topPadding = getPaddingTop();

        float width = canvas.getWidth() - leftPadding - rightPadding;
        float height = canvas.getHeight() - topPadding - getPaddingBottom()
                - backgroundPaint.getTextSize() + 0.5f;

        if (getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
            leftPadding = getPaddingEnd();
            labelLeftPadding = leftPadding + width + maxLabelWidth * 0.25f;
        }

        if (lastWidth != width || lastHeight != height) {
            regenerate = true;

            lastWidth = width;
            lastHeight = height;
        }

        regenerateGraphs(leftPadding, topPadding, width, height);
        drawLegend(canvas, labelLeftPadding, leftPadding, topPadding, width, height);
        drawGraphs(canvas);

        canvas.restore();
        if (missingAnimations()) invalidate();
    }

    private void regenerateGraphs(float leftPadding, float topPadding, float width, float height) {
        if (regenerate) {
            for (int j = 0; j < 2; j++) {
                circlePath[j].reset();
                graphPath[j].reset();
            }

            backgroundPath.reset();

            if (generateLabels) {
                labels = new String[dataPoints[0].size() + 1];
                for (int i = 0; i < labels.length; i++) {
                    labels[i] = "" + i;
                }
            }

            for (int i = 0; i <= dataPoints[0].size(); i++) {
                float xl = width * (((float) i) / dataPoints[0].size()) + leftPadding;
                backgroundPath.moveTo(xl, topPadding);
                backgroundPath.lineTo(xl, topPadding + height);
            }

            for (int i = 0; i <= 10; i++) {
                float yl = ((float) i / 10.f) * height + topPadding;
                backgroundPath.moveTo(leftPadding, yl);
                backgroundPath.lineTo(leftPadding + width, yl);
            }

            for (int j = 0; j < 2; j++) {
                if (dataPoints[j] != null) {
                    float x = leftPadding;
                    float y = height * getDataPoint(0, j) + topPadding;

                    graphPath[j].moveTo(x, y);
                    circlePath[j].addCircle(x, y, 10, Path.Direction.CW);

                    for (int i = 1; i < dataPoints[j].size(); i++) {
                        x = width * (((float) i + 1) / dataPoints[j].size()) + leftPadding;
                        y = height * getDataPoint(i, j) + topPadding;

                        graphPath[j].lineTo(x, y);
                        circlePath[j].addCircle(x, y, 10, Path.Direction.CW);
                    }
                }
            }
            regenerate = false;
        }
    }

    private void drawGraphs(Canvas canvas) {
        for (int j = 0; j < graphPath.length; j++) {
            canvas.drawPath(graphPath[j], linePaint[j]);
            canvas.drawPath(circlePath[j], circlePaint[j]);
        }
    }

    private void drawLegend(Canvas canvas, float labelLeftPadding, float leftPadding, float topPadding, float width, float height) {
        if (drawLegend) {
            canvas.drawPath(backgroundPath, backgroundPaint);

            // draw bottom legend
            for (int i = 0; i <= dataPoints[0].size(); i++) {
                float xl = width * (((float) i) / dataPoints[0].size()) + leftPadding;
                backgroundPaint.getTextBounds(labels[i], 0, labels[i].length(), textBoundaries);
                canvas.drawText(labels[i],
                        xl - (textBoundaries.width() / 2),
                        height + topPadding + backgroundPaint.getTextSize() * 1.5f,
                        backgroundPaint);
            }

            // draw side legend
            for (int i = 0; i <= 10; i++) {
                float step = ((float) i / 10.f);
                float yl = step * height + topPadding
                        - (backgroundPaint.ascent() + backgroundPaint.descent()) * 0.5f;

                canvas.drawText(verticalLabels[i],
                        labelLeftPadding,
                        yl,
                        backgroundPaint);
            }
        }
    }

    private float getDataPoint(int i, int index) {
        float data = (dataPoints[index].get(i) - minValue) / verticalDelta;
        return invertVerticalAxis ? 1.f - data : data;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        if (zooming) {
            invalidate();
            zooming = false;
            return true;
        }

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dragX = event.getX();
                dragY = event.getY();

                getParent().requestDisallowInterceptTouchEvent(true);
                dragged = false;
                return true;

            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;

            case MotionEvent.ACTION_MOVE:
                float newX = event.getX();
                float newY = event.getY();

                scrollScreen(dragX - newX, dragY - newY);

                dragX = newX;
                dragY = newY;
                dragged = true;
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if there is any animation that has not finished.
     */
    private boolean missingAnimations() {
        if (Math.abs(scrollXTarget - scrollX) > ANIM_THRESHOLD) return true;
        if (Math.abs(scrollYTarget - scrollY) > ANIM_THRESHOLD) return true;

        return false;
    }

    /**
     * Execute logic iterations and interpolate between current and next logic iteration
     */
    private void animateLogic() {
        long currentTime = SystemClock.elapsedRealtime();
        accTime += currentTime - timeStart;
        timeStart = currentTime;

        while (accTime > TIME_THRESHOLD) {
            scrollX += (scrollXTarget - scrollX) / 4.f;
            scrollY += (scrollYTarget - scrollY) / 4.f;
            accTime -= TIME_THRESHOLD;
        }

        float factor = ((float) accTime) / TIME_THRESHOLD;
        float nextScrollX = scrollX + (scrollXTarget - scrollX) / 4.f;
        float nextScrollY = scrollY + (scrollYTarget - scrollY) / 4.f;

        frScrollX = scrollX * (1.f - factor) + nextScrollX * factor;
        frScrollY = scrollY * (1.f - factor) + nextScrollY * factor;
    }

    /**
     * scroll screen by dx, dy and trigger a redraw cycle.
     */
    private void scrollScreen(float dx, float dy) {
        scrollXTarget += dx;
        scrollYTarget += dy;

        if (scrollXTarget < 0) scrollXTarget = 0;
        if (scrollYTarget < 0) scrollYTarget = 0;

        if (scrollXTarget > getWidth() * scale - getWidth()) {
            scrollXTarget = getWidth() * scale - getWidth();
        }

        if (scrollYTarget > getHeight() * scale - getHeight()) {
            scrollYTarget = getHeight() * scale - getHeight();
        }

        invalidate();
    }
}
