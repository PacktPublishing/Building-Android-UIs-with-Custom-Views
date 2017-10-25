package com.rrafols.packt.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import java.text.DecimalFormat;

public class Chart extends View {
    private float[] dataPoints;
    private Paint linePaint;
    private Paint circlePaint;
    private Paint backgroundPaint;
    private Path graphPath;
    private Path circlePath;
    private Path backgroundPath;
    private boolean regenerate;
    private float lastWidth;
    private float lastHeight;
    private Rect textBoundaries;
    private float minValue;
    private float verticalDelta;
    private DecimalFormat decimalFormat;
    private String[] labels;
    private String[] verticalLabels;
    private boolean invertVerticalAxis;
    private boolean drawLegend;

    public Chart(Context context, AttributeSet attrs) {
        super(context, attrs);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(0xffffffff);
        linePaint.setStrokeWidth(8.f);
        linePaint.setStyle(Paint.Style.STROKE);

        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(0xffff2020);
        circlePaint.setStyle(Paint.Style.FILL);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xffFFFF80);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setPathEffect(new DashPathEffect(new float[] {5, 5}, 0));
        backgroundPaint.setTextSize(20.f);

        graphPath = new Path();
        circlePath = new Path();
        backgroundPath = new Path();
        lastWidth = -1;
        lastHeight = -1;

        textBoundaries = new Rect();
        decimalFormat = new DecimalFormat("#.##");
        verticalLabels = new String[11];

        invertVerticalAxis = false;
        drawLegend = true;
    }

    // make a copy of the data as the original array content might change.
    public void setDataPoints(float[] originalData) {
        dataPoints = new float[originalData.length];

        float maxValue = Float.MIN_VALUE;
        minValue = Float.MAX_VALUE;
        for (int i = 0; i < dataPoints.length; i++) {
            dataPoints[i] = originalData[i];
            if (dataPoints[i] < minValue) minValue = dataPoints[i];
            if (dataPoints[i] > maxValue) maxValue = dataPoints[i];
        }

        verticalDelta = maxValue - minValue;

        for (int i = 0; i < dataPoints.length; i++) {
            dataPoints[i] = (dataPoints[i] - minValue) / verticalDelta;
        }

        regenerate = true;
        postInvalidate();
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawARGB(255,0 ,0 ,0);

        float maxLabelWidth = 0.f;
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

        if (regenerate) {
            circlePath.reset();
            graphPath.reset();
            backgroundPath.reset();

            if (labels == null) {
                labels = new String[dataPoints.length + 1];
                for (int i = 0; i < labels.length; i++) {
                    labels[i] = "" + i;
                }
            }

            for (int i = 0; i <= dataPoints.length; i++) {
                float xl = width * (((float) i) / dataPoints.length) + leftPadding;
                backgroundPath.moveTo(xl, topPadding);
                backgroundPath.lineTo(xl, topPadding + height);
            }

            for (int i = 0; i <= 10; i++) {
                float yl = ((float) i / 10.f) * height + topPadding;
                backgroundPath.moveTo(leftPadding, yl);
                backgroundPath.lineTo(leftPadding + width, yl);
            }

            float x = leftPadding;
            float y = height * getDataPoint(0) + topPadding;

            graphPath.moveTo(x, y);
            circlePath.addCircle(x, y, 10, Path.Direction.CW);

            for (int i = 1; i < dataPoints.length; i++) {
                x = width * (((float) i + 1) / dataPoints.length) + leftPadding;
                y = height * getDataPoint(i) + topPadding;

                graphPath.lineTo(x, y);
                circlePath.addCircle(x, y, 10, Path.Direction.CW);
            }

            regenerate = false;
        }

        if (drawLegend) {
            canvas.drawPath(backgroundPath, backgroundPaint);

            // draw bottom legend
            for (int i = 0; i <= dataPoints.length; i++) {
                float xl = width * (((float) i) / dataPoints.length) + leftPadding;
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
                        + backgroundPaint.getTextSize() * 0.5f;

                canvas.drawText(verticalLabels[i],
                        labelLeftPadding,
                        yl,
                        backgroundPaint);
            }
        }

        canvas.drawPath(graphPath, linePaint);
        canvas.drawPath(circlePath, circlePaint);
    }

    private float getDataPoint(int i) {
        float data = dataPoints[i];
        return invertVerticalAxis ? 1.f - data : data;
    }
}
