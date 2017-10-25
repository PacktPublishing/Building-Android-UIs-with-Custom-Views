package com.packt.rrafols.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLDrawer extends GLSurfaceView {
    private static final String TAG = GLDrawer.class.toString();

    private GLRenderer glRenderer;

    public GLDrawer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setEGLContextClientVersion(2);

        glRenderer = new GLRenderer();
        setRenderer(glRenderer);
    }

    class GLRenderer implements GLSurfaceView.Renderer {
        private float quadCoords[] = {
                -1.f, -1.f, -1.0f,
                -1.f,  1.f, -1.0f,
                 1.f,  1.f, -1.0f,
                 1.f, -1.f, -1.0f,

                -1.f, -1.f,  1.0f,
                -1.f,  1.f,  1.0f,
                 1.f,  1.f,  1.0f,
                 1.f, -1.f,  1.0f};

        private short[] index = {
                0, 1, 2,        // front
                0, 2, 3,        // front
                4, 5, 6,        // back
                4, 6, 7,        // back
                0, 4, 7,        // top
                0, 3, 7,        // top
                1, 5, 6,        // bottom
                1, 2, 6,        // bottom
                0, 4, 5,        // left
                0, 1, 5,        // left
                3, 7, 6,        // right
                3, 2, 6         // right
        };

        private float texCoords[] = {
                1.f, 1.f,
                1.f, 0.f,
                0.f, 0.f,
                0.f, 1.f,

                1.f, 1.f,
                1.f, 0.f,
                0.f, 0.f,
                0.f, 1.f,
        };

        private final float[] mMVPMatrix = new float[16];
        private final float[] mProjectionMatrix = new float[16];
        private final float[] mViewMatrix = new float[16];

        // Source (updated with texture interpolation)
        // https://developer.android.com/training/graphics/opengl/draw.html
        private final String vertexShaderCode =
                "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "attribute vec2 aTex;" +
                "varying vec2 vTex;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "  vTex = aTex;" +
                "}";

        private final String fragmentShaderCode =
                "precision mediump float;" +
                "uniform sampler2D sTex;" +
                "varying vec2 vTex;" +
                "void main() {" +
                "  gl_FragColor = texture2D(sTex, vTex);" +
                "}";

        private FloatBuffer vertexBuffer;
        private ShortBuffer indexBuffer;
        private FloatBuffer texBuffer;
        private int shaderProgram;
        private int textureId;
        private float angle = 0.f;
        private long startTime;

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            initBuffers();
            initShaders();

            textureId = loadTexture(R.drawable.texture);

            startTime = SystemClock.elapsedRealtime();
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

            ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * (Float.SIZE / 8));
            tbb.order(ByteOrder.nativeOrder());

            texBuffer = tbb.asFloatBuffer();
            texBuffer.put(texCoords);
            texBuffer.position(0);
        }

        private void initShaders() {
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            shaderProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(shaderProgram, vertexShader);
            GLES20.glAttachShader(shaderProgram, fragmentShader);
            GLES20.glLinkProgram(shaderProgram);
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);

            float ratio = (float) width / height;
            Matrix.frustumM(mProjectionMatrix, 0, -ratio * 2, ratio * 2, -2, 2, 2, 7);
        }

        @Override
        public void onDrawFrame(GL10 unused) {
            angle = ((float) SystemClock.elapsedRealtime() - startTime) * 0.02f;
            GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            Matrix.setLookAtM(mViewMatrix, 0,
                    0, 0, -4,
                    0f, 0f, 0f,
                    0f, 1.0f, 0.0f);

            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
            Matrix.rotateM(mMVPMatrix, 0, angle, 1.f, 1.f, 1.f);

            GLES20.glUseProgram(shaderProgram);

            int positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");

            GLES20.glVertexAttribPointer(positionHandle, 3,
                    GLES20.GL_FLOAT, false,
                    0, vertexBuffer);

            int texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "aTex");
            GLES20.glVertexAttribPointer(texCoordHandle, 2,
                    GLES20.GL_FLOAT, false,
                    0, texBuffer);

            int mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");

            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

            int texHandle = GLES20.glGetUniformLocation(shaderProgram, "sTex");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(texHandle, 0);

            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glEnableVertexAttribArray(texHandle);
            GLES20.glEnableVertexAttribArray(positionHandle);
            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES, index.length,
                    GLES20.GL_UNSIGNED_SHORT, indexBuffer);

            GLES20.glDisableVertexAttribArray(positionHandle);
            GLES20.glDisableVertexAttribArray(texHandle);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        }
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
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

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
}
