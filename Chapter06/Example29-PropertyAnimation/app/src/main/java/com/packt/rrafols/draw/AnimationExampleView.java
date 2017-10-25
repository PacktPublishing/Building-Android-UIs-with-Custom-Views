package com.packt.rrafols.draw;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

public class AnimationExampleView extends View {
    private static final String TAG = AnimationExampleView.class.getName();

    private static final int BACKGROUND_COLOR = 0xff202040;
    private static final int FOREGROUND_COLOR = 0xffffffff;
    private static final int QUAD_SIZE = 50;

    private Paint paint;
    private int previousVisibility;
    private float[] angle;
    private float canvasAngle;

    public AnimationExampleView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(FOREGROUND_COLOR);
        paint.setTextSize(48.f);

        angle = new float[4];
        for (int i = 0; i < angle.length; i++) {
            angle[i] = 0.f;
        }

        canvasAngle = 0.f;

        //top left
        final ValueAnimator angleAnimatorTL = ValueAnimator.ofFloat(0, 360.f);
        angleAnimatorTL.setRepeatMode(ValueAnimator.REVERSE);
        angleAnimatorTL.setRepeatCount(ValueAnimator.INFINITE);
        angleAnimatorTL.setDuration(1500);
        angleAnimatorTL.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                angle[0] = (float) angleAnimatorTL.getAnimatedValue();
                invalidate();
            }
        });

        //top right
        final ValueAnimator angleAnimatorTR = ValueAnimator.ofFloat(0, 360.f);
        angleAnimatorTR.setInterpolator(new DecelerateInterpolator());
        angleAnimatorTR.setRepeatMode(ValueAnimator.RESTART);
        angleAnimatorTR.setRepeatCount(ValueAnimator.INFINITE);
        angleAnimatorTR.setDuration(1500);
        angleAnimatorTR.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                angle[1] = (float) angleAnimatorTR.getAnimatedValue();
                invalidate();
            }
        });

        //bottom left
        final ValueAnimator angleAnimatorBL = ValueAnimator.ofFloat(0, 360.f);
        angleAnimatorBL.setInterpolator(new AccelerateDecelerateInterpolator());
        angleAnimatorBL.setRepeatMode(ValueAnimator.RESTART);
        angleAnimatorBL.setRepeatCount(ValueAnimator.INFINITE);
        angleAnimatorBL.setDuration(1500);
        angleAnimatorBL.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                angle[2] = (float) angleAnimatorBL.getAnimatedValue();
                invalidate();
            }
        });

        //bottom right
        final ValueAnimator angleAnimatorBR = ValueAnimator.ofFloat(0, 360.f);
        angleAnimatorBR.setInterpolator(new OvershootInterpolator());
        angleAnimatorBR.setRepeatMode(ValueAnimator.REVERSE);
        angleAnimatorBR.setRepeatCount(ValueAnimator.INFINITE);
        angleAnimatorBR.setDuration(1500);
        angleAnimatorBR.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                angle[3] = (float) angleAnimatorBR.getAnimatedValue();
                invalidate();
            }
        });

        ObjectAnimator canvasAngleAnimator = ObjectAnimator.ofFloat(this, "canvasAngle", -10.f, 10.f);
        canvasAngleAnimator.setDuration(3000);
        canvasAngleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        canvasAngleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        canvasAngleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });

        angleAnimatorTL.start();
        angleAnimatorTR.start();
        angleAnimatorBL.start();
        angleAnimatorBR.start();
        canvasAngleAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.rotate(canvasAngle, getWidth() / 2, getHeight() / 2);

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
        canvas.restore();
    }

    public void setCanvasAngle(float canvasAngle) {
        this.canvasAngle = canvasAngle;
    }

    public float getCanvasAngle() {
        return canvasAngle;
    }
}