package com.packt.rrafols.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Scroller;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLDrawer extends GLSurfaceView {
    private static final String TAG = GLDrawer.class.toString();
    private GLRenderer glRenderer;
    private GestureDetectorCompat gestureDetector;
    private Scroller scroller;
    private OnMenuClickedListener listener;

    public GLDrawer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setEGLContextClientVersion(2);

        listener = null;
        gestureDetector = new GestureDetectorCompat(context, new MenuGestureListener());
        scroller = new Scroller(context);
        glRenderer = new GLRenderer();
        setRenderer(glRenderer);
    }

    public void setOnMenuClickedListener(OnMenuClickedListener listener) {
        this.listener = listener;
    }

    public void setColors(int[] faceColors) {
        glRenderer.setColors(faceColors);
    }

    class GLRenderer implements GLSurfaceView.Renderer {
        private float quadCoords[] = {
                -1.f, -1.f, -1.0f,  // 0
                -1.f,  1.f, -1.0f,  // 1
                 1.f,  1.f, -1.0f,  // 2
                 1.f, -1.f, -1.0f,  // 3

                -1.f, -1.f,  1.0f,  // 4
                -1.f,  1.f,  1.0f,  // 5
                 1.f,  1.f,  1.0f,  // 6
                 1.f, -1.f,  1.0f,   // 7

                -1.f, -1.f, -1.0f,  // 8 - 0
                -1.f, -1.f,  1.0f,  // 9 - 4
                 1.f, -1.f,  1.0f,  // 10 - 7
                 1.f, -1.f, -1.0f,  // 11 - 3

                -1.f,  1.f, -1.0f,  // 12 - 1
                -1.f,  1.f,  1.0f,  // 13 - 5
                 1.f,  1.f,  1.0f,  // 14 - 6
                 1.f,  1.f, -1.0f,  // 15 - 2

                -1.f, -1.f, -1.0f,  // 16 - 0
                -1.f, -1.f,  1.0f,  // 17 - 4
                -1.f,  1.f,  1.0f,  // 18 - 5
                -1.f,  1.f, -1.0f,  // 19 - 1

                 1.f, -1.f, -1.0f,  // 20 - 3
                 1.f, -1.f,  1.0f,  // 21 - 7
                 1.f,  1.f,  1.0f,  // 22 - 6
                 1.f,  1.f, -1.0f   // 23 - 2
        };

        private short[] index = {
                0, 1, 2,        // front
                0, 2, 3,        // front
                4, 5, 6,        // back
                4, 6, 7,        // back
                8, 9,10,        // top
                8,11,10,        // top
               12,13,14,        // bottom
               12,15,14,        // bottom
               16,17,18,        // left
               16,19,18,        // left
               20,21,22,        // right
               20,23,22         // right
        };

        float colors[] = {
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,

                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 0.0f, 1.0f,

                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 1.0f,

                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f
        };

        private final float[] mMVPMatrix = new float[16];
        private final float[] mProjectionMatrix = new float[16];
        private final float[] mViewMatrix = new float[16];

        // Source (updated with texture interpolation)
        // https://developer.android.com/training/graphics/opengl/draw.html
        private final String vertexShaderCode =
                "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "attribute vec4 aColor;" +
                "varying vec4 vColor;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "  vColor = aColor;" +
                "}";

        private final String fragmentShaderCode =
                "precision mediump float;" +
                "varying vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}";

        private FloatBuffer vertexBuffer;
        private ShortBuffer indexBuffer;
        private FloatBuffer colorBuffer;
        private int shaderProgram;

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            initBuffers();
            initShaders();
        }

        private void initBuffers() {
            ByteBuffer vbb = ByteBuffer.allocateDirect(quadCoords.length * (Float.SIZE / 8));
            vbb.order(ByteOrder.nativeOrder());

            vertexBuffer = vbb.asFloatBuffer();
            vertexBuffer.put(quadCoords);
            vertexBuffer.position(0);

            ByteBuffer ibb = ByteBuffer.allocateDirect(index.length * (Short.SIZE / 8));
            ibb.order(ByteOrder.nativeOrder());

            indexBuffer = ibb.asShortBuffer();
            indexBuffer.put(index);
            indexBuffer.position(0);

            ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * (Float.SIZE / 8));
            cbb.order(ByteOrder.nativeOrder());

            colorBuffer = cbb.asFloatBuffer();
            colorBuffer.put(colors);
            colorBuffer.position(0);
        }

        private void initShaders() {
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            shaderProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(shaderProgram, vertexShader);
            GLES20.glAttachShader(shaderProgram, fragmentShader);
            GLES20.glLinkProgram(shaderProgram);
        }

        private float[] hexToRGBA(int color) {
            float[] out = new float[4];

            int a = (color >> 24) & 0xff;
            int r = (color >> 16) & 0xff;
            int g = (color >>  8) & 0xff;
            int b = (color      ) & 0xff;

            out[0] = ((float) r) / 255.f;
            out[1] = ((float) g) / 255.f;
            out[2] = ((float) b) / 255.f;
            out[3] = ((float) a) / 255.f;
            return out;
        }

        private void setColors(int[] faceColors) {
            colors = new float[4 * 4 * faceColors.length];
            int wOffset = 0;
            for (int faceColor : faceColors) {
                float[] color = hexToRGBA(faceColor);
                for(int j = 0; j < 4; j++) {
                    colors[wOffset++] = color[0];
                    colors[wOffset++] = color[1];
                    colors[wOffset++] = color[2];
                    colors[wOffset++] = color[3];
                }
            }
            ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * (Float.SIZE / 8));
            cbb.order(ByteOrder.nativeOrder());

            colorBuffer = cbb.asFloatBuffer();
            colorBuffer.put(colors);
            colorBuffer.position(0);
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            Matrix.perspectiveM(mProjectionMatrix, 0, 90, ratio, 0.1f, 7.f);
        }

        @Override
        public void onDrawFrame(GL10 unused) {
            scroller.computeScrollOffset();
            if (scroller.isFinished()) {
                int lastX = scroller.getCurrX();
                int modulo = lastX % 90;
                int snapX = (lastX / 90) * 90;
                if (modulo >= 45) snapX += 90;
                if (modulo <- 45) snapX -= 90;

                if (lastX != snapX) {
                    scroller.startScroll(lastX, 0, snapX - lastX, 0);
                }
            }

            GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            Matrix.setLookAtM(mViewMatrix, 0,
                    0, 0, -3,
                    0f, 0f, 0f,
                    0f, 1.0f, 0.0f);


            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
            Matrix.rotateM(mMVPMatrix, 0, scroller.getCurrX(), 0.f, 1.f, 0.f);
            Matrix.rotateM(mMVPMatrix, 0, 5.f, 1.f, 0.f, 0.f);


            GLES20.glUseProgram(shaderProgram);

            int positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");

            GLES20.glVertexAttribPointer(positionHandle, 3,
                    GLES20.GL_FLOAT, false,
                    0, vertexBuffer);

            int colorHandle = GLES20.glGetAttribLocation(shaderProgram, "aColor");
            GLES20.glVertexAttribPointer(colorHandle, 4,
                    GLES20.GL_FLOAT, false,
                    4 * 4, colorBuffer);

            int mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");

            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glEnableVertexAttribArray(colorHandle);
            GLES20.glEnableVertexAttribArray(positionHandle);
            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES, index.length,
                    GLES20.GL_UNSIGNED_SHORT, indexBuffer);

            GLES20.glDisableVertexAttribArray(positionHandle);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private int loadTexture(int resId) {
        final int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);

        if (textureIds[0] == 0) return -1;

        // do not scale the bitmap depending on screen density
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        final Bitmap textureBitmap = BitmapFactory.decodeResource(getResources(), resId, options);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0);
        textureBitmap.recycle();

        return textureIds[0];
    }

    // Source:
    // https://developer.android.com/training/graphics/opengl/draw.html
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    class MenuGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            scroller.forceFinished(true);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            scroller.computeScrollOffset();
            int angle = scroller.getCurrX();
            int face = (angle / 90) % 4;
            if (face < 0) face += 4;

            if (listener != null) listener.menuClicked(face);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            scroller.computeScrollOffset();
            int lastX = scroller.getCurrX();

            scroller.forceFinished(true);
            scroller.startScroll(lastX, 0, -(int) (distanceX + 0.5f), 0);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            scroller.computeScrollOffset();
            int lastX = scroller.getCurrX();

            scroller.forceFinished(true);
            scroller.fling(lastX, 0, (int) (velocityX/4.f), 0, -360*100, 360*100, 0,0 );
            return true;
        }
    }

    interface OnMenuClickedListener {
        void menuClicked(int option);
    }
}
