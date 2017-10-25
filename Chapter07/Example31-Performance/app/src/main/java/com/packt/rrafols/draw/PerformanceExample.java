package com.packt.rrafols.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.DataInputStream;

public class PerformanceExample extends View {
    private static final String TAG = PerformanceExample.class.getName();

    private int frames;
    private long timeStart;
    private int imageWidth;
    private int imageHeight;
    private Bitmap bitmap;
    private int[] rgbData = null;
    private byte[] yuvData = null;

    private static int[] luminance;
    private static int[] chromaR;
    private static int[] chromaGU;
    private static int[] chromaGV;
    private static int[] chromaB;

    private static int[] clipValuesR;
    private static int[] clipValuesG;
    private static int[] clipValuesB;

    public PerformanceExample(final Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        frames = 0;
        timeStart = -1;
        new Thread(new Runnable() {
            @Override
            public void run() {

                DataInputStream dis = null;
                try {
                    dis = new DataInputStream(context.getAssets().open("image.yuv"));
                    imageWidth = dis.readInt();
                    imageHeight = dis.readInt();

                    bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
                    rgbData = new int[imageWidth * imageHeight];
                    byte[] tmpData = new byte[imageWidth * imageHeight * 2];
                    dis.readFully(tmpData);

                    precalcTables();

                    yuvData = tmpData;
                } catch(Exception e) {
                    Log.e(TAG, "Error opening YUV image", e);
                } finally {
                    try { dis.close(); } catch (Exception e) {}
                }


                postInvalidate();
            }
        }).start();
    }

    private static void precalcTables() {
        luminance = new int[256];
        for (int i = 0; i < luminance.length; i++) {
            luminance[i] = ((298 * (i - 16)) >> 8) + 300;
        }

        chromaR = new int[256];
        chromaGU = new int[256];
        chromaGV = new int[256];
        chromaB = new int[256];
        for (int i = 0; i < 256; i++) {
            chromaR[i]  =  (517 * (i - 128)) >> 8;
            chromaGU[i] = (-100 * (i - 128)) >> 8;
            chromaGV[i] = (-208 * (i - 128)) >> 8;
            chromaB[i]  = (409 * (i - 128)) >> 8;
        }

        clipValuesR = new int[1024];
        clipValuesG = new int[1024];
        clipValuesB = new int[1024];
        for (int i = 0; i < 1024; i++) {
            clipValuesR[i] = 0xFF000000 | (min(max(i - 300, 0), 255) << 16);
            clipValuesG[i] = min(max(i - 300, 0), 255) << 8;
            clipValuesB[i] = min(max(i - 300, 0), 255);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (timeStart == -1) {
            timeStart = SystemClock.elapsedRealtime();
        } else {
            long tdiff = SystemClock.elapsedRealtime() - timeStart;
            if (tdiff != 0) {
                float fps = ((float) frames * 1000.f) / tdiff;
                Log.d(TAG, "FPS: " + fps);
            }
        }

        yuv2rgb(imageWidth, imageHeight, yuvData, rgbData);
        bitmap.setPixels(rgbData, 0, imageWidth, 0, 0, imageWidth, imageHeight);

        canvas.drawBitmap(bitmap, 0.f, 0.f, null);

        frames++;
        invalidate();
    }

    //    original example based on
    //    http://sourceforge.jp/projects/nyartoolkit-and/
    private static void yuv2rgb(int width, int height, byte[] yuvData, int[] rgbData) {
        int uvOffset = width * height;
        int offset = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j += 2) {
                int y0 = yuvData[offset] & 0xff;

                int u = yuvData[uvOffset++] & 0xff;
                int v = yuvData[uvOffset++] & 0xff;

                int chR = chromaR[u];
                int chG = chromaGV[v] + chromaGU[u];
                int chB = chromaB[v];

                int lum = luminance[y0];
                int nR = clipValuesR[lum + chR];
                int nG = clipValuesG[lum + chG];
                int nB = clipValuesB[lum + chB];
                rgbData[offset++] =  nR | nG | nB;

                int y1 = yuvData[offset] & 0xff;
                lum = luminance[y1];
                nR = clipValuesR[lum + chR];
                nG = clipValuesG[lum + chG];
                nB = clipValuesB[lum + chB];
                rgbData[offset++] = nR | nG | nB;
            }
        }
    }

    private static int min(int x, int y) {
        return (x < y) ? x : y;
    }

    private static int max(int x, int y) {
        return (x > y) ? x : y;
    }
}