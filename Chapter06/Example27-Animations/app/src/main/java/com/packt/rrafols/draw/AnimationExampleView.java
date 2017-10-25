package com.packt.rrafols.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

public class AnimationExampleView extends View {
    private static final String TAG = AnimationExampleView.class.getName();

    private static final int BACKGROUND_COLOR = 0xff205020;
    private static final int FOREGROUND_COLOR = 0xffffffff;
    private static final int QUAD_SIZE = 50;

    private Paint paint;
    private int previousVisibility;
    private float[] angle;
    private long timeStartMillis;
    private long timeStartNanos;
    private long timeStartElapsed;
    private long invisibleTimeStart;

    public AnimationExampleView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(FOREGROUND_COLOR);
        paint.setTextSize(48.f);

        angle = new float[4];
        for (int i = 0; i < 4; i++) {
            angle[i] = 0.f;
        }

        timeStartMillis = -1;
        timeStartNanos = -1;
        timeStartElapsed = -1;
        previousVisibility = View.GONE;
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        // avoid doing this check before View is even visible
        if (timeStartElapsed != -1) {
            if ((visibility == View.INVISIBLE || visibility == View.GONE) &&
                    previousVisibility == View.VISIBLE) {

                invisibleTimeStart = SystemClock.elapsedRealtime();
            }

            if ((previousVisibility == View.INVISIBLE || previousVisibility == View.GONE) &&
                    visibility == View.VISIBLE) {

                timeStartElapsed += SystemClock.elapsedRealtime() - invisibleTimeStart;
            }
        } else {
            timeStartMillis = System.currentTimeMillis();
            timeStartNanos = System.nanoTime();
            timeStartElapsed = SystemClock.elapsedRealtime();
        }

        previousVisibility = visibility;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (timeStartMillis == -1) timeStartMillis = System.currentTimeMillis();
        if (timeStartNanos == -1) timeStartNanos = System.nanoTime();
        if (timeStartElapsed == -1) timeStartElapsed = SystemClock.elapsedRealtime();

        angle[0] += 0.2f;
        angle[1] = (System.currentTimeMillis() - timeStartMillis) * 0.02f;
        angle[2] = (System.nanoTime() - timeStartNanos) * 0.02f * 0.000001f;
        angle[3] = (SystemClock.elapsedRealtime() - timeStartElapsed) * 0.02f;

        canvas.drawColor(BACKGROUND_COLOR);

        int width = getWidth();
        int height = getHeight();

        // draw 4 quads on the screen:
        int wh = width / 2;
        int hh = height / 2;

        int qs = (wh * QUAD_SIZE) / 100;

        // top left
        canvas.save();
        canvas.translate(
                wh / 2 - qs / 2,
                hh / 2 - qs / 2);

        canvas.rotate(angle[0], qs / 2.f, qs / 2.f);
        canvas.drawRect(0, 0, qs, qs, paint);
        canvas.restore();

        // top right
        canvas.save();
        canvas.translate(
                wh + wh / 2 - qs / 2,
                hh / 2 - qs / 2);

        canvas.rotate(angle[1], qs / 2.f, qs / 2.f);
        canvas.drawRect(0, 0, qs, qs, paint);
        canvas.restore();

        // bottom left
        canvas.save();
        canvas.translate(
                wh / 2 - qs / 2,
                hh + hh / 2 - qs / 2);

        canvas.rotate(angle[2], qs / 2.f, qs / 2.f);
        canvas.drawRect(0, 0, qs, qs, paint);
        canvas.restore();

        // bottom right
        canvas.save();
        canvas.translate(
                wh + wh / 2 - qs / 2,
                hh + hh / 2 - qs / 2);

        canvas.rotate(angle[3], qs / 2.f, qs / 2.f);
        canvas.drawRect(0, 0, qs, qs, paint);
        canvas.restore();

        canvas.drawText("a: " + angle[0], 16, hh - 16, paint);
        canvas.drawText("a: " + angle[1], wh + 16, hh - 16, paint);
        canvas.drawText("a: " + angle[2], 16, height - 16, paint);
        canvas.drawText("a: " + angle[3], wh + 16, height - 16, paint);

        postInvalidateDelayed(10);
    }
}