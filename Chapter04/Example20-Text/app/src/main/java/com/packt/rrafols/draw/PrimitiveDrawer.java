package com.packt.rrafols.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class PrimitiveDrawer extends View {
    private static final String TAG = PrimitiveDrawer.class.getName();

    private static final int BACKGROUND_COLOR = 0xff000040;
    private TextPaint paint;
    private static String LONG_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Morbi eleifend " +
                                      "eget justo eget pulvinar. Interdum et malesuada fames ac ante ipsum primis" +
                                      "in faucibus. Donec egestas lacus quam, dignissim varius mi finibus in. " +
                                      "Maecenas congue a libero sed rhoncus. Ut ac vulputate diam. Sed aliquam " +
                                      "vehicula lectus nec blandit. Phasellus elit orci, feugiat nec arcu eget, " +
                                      "congue varius purus. Nulla sed auctor metus, et tempus sapien. Fusce aliquam " +
                                      "dignissim hendrerit. Donec interdum mattis nulla, eget scelerisque neque mattis " +
                                      "vitae. Maecenas fringilla neque vel enim consectetur, eget aliquet sem tempus. " +
                                      "Aliquam mattis nunc neque. Aenean augue lorem, pulvinar id enim in, facilisis " +
                                      "consectetur quam. Praesent eget fringilla arcu. Vestibulum consequat augue quis " +
                                      "iaculis egestas. Suspendisse molestie ante nisi, vel rutrum nisi euismod a.";

    private StaticLayout layout;


    public PrimitiveDrawer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        paint = new TextPaint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(0xffffffff);
        paint.setStrokeWidth(1.f);
        paint.setTextSize(35.f);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private static final float[] getTextSize(String str, Paint paint) {
        float[] out = new float[2];
        Rect boundaries = new Rect();
        paint.getTextBounds(str, 0, str.length(), boundaries);

        out[0] = paint.measureText(str);
        out[1] = boundaries.height();
        return out;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);


        // create a layout of half the width of the View
        if (layout == null) {
            layout = new StaticLayout(
                    LONG_TEXT,
                    0,
                    LONG_TEXT.length(),
                    paint,
                    (right - left) / 2,
                    Layout.Alignment.ALIGN_NORMAL,
                    1.f,
                    1.f,
                    true);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(BACKGROUND_COLOR);

        canvas.save();
        // center the layout on the View
        canvas.translate(canvas.getWidth()/4, 0);
        layout.draw(canvas);
        canvas.restore();
    }
}