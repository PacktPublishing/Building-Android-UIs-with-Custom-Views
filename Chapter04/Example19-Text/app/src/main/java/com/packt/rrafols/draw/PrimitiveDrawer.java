package com.packt.rrafols.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class PrimitiveDrawer extends View {
    private static final String TAG = PrimitiveDrawer.class.getName();

    private static final int BACKGROUND_COLOR = 0xff000040;

    private ArrayList<Float> points;
    private int currentIndex;
    private Paint pathPaint;
    private Paint pointsPaint;
    private Path path;

    public PrimitiveDrawer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        pathPaint = new Paint();
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setAntiAlias(true);
        pathPaint.setColor(0xffffffff);
        pathPaint.setStrokeWidth(5.f);
        pathPaint.setTextSize(50.f);
        pathPaint.setTextAlign(Paint.Align.CENTER);


        pointsPaint = new Paint();
        pointsPaint.setStyle(Paint.Style.STROKE);
        pointsPaint.setAntiAlias(true);
        pointsPaint.setColor(0xffff0000);
        pointsPaint.setStrokeCap(Paint.Cap.ROUND);
        pointsPaint.setStrokeWidth(40.f);

        points = new ArrayList<>();
        path = new Path();
        currentIndex = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(points.size() == 0) {
            points.add(getWidth() / 2.f);
            points.add(getHeight() / 2.f);
        }

        while(points.size() - currentIndex >= 6) {
            float x1 = points.get(currentIndex);
            float y1 = points.get(currentIndex + 1);

            float x2 = points.get(currentIndex + 2);
            float y2 = points.get(currentIndex + 3);

            float x3 = points.get(currentIndex + 4);
            float y3 = points.get(currentIndex + 5);

            if (currentIndex == 0) path.moveTo(x1, y1);
            path.cubicTo(x1, y1, x2, y2, x3, y3);
            currentIndex += 6;
        }

        canvas.drawColor(BACKGROUND_COLOR);
        canvas.drawPath(path, pathPaint);

        for(int i = 0; i < points.size() / 2; i++) {
            float x = points.get(i * 2    );
            float y = points.get(i * 2 + 1);
            canvas.drawPoint(x, y, pointsPaint);
        }

        canvas.drawTextOnPath("Building Android UIs with Custom Views", path, 0, 0, pathPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            points.add(event.getX());
            points.add(event.getY());

            invalidate();
        }

        return super.onTouchEvent(event);
    }
}