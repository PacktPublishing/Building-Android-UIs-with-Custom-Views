package com.packt.rrafols.draw;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
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
                -1.f, -1.f, 0.0f,
                -1.f,  1.f, 0.0f,
                 1.f,  1.f, 0.0f,
                 1.f, -1.f, 0.0f };

        private short[] index = {
                0, 1, 2,
                0, 2, 3
        };

        float color[] = { 0.2f, 0.2f, 0.9f, 1.0f };

        private final float[] mMVPMatrix = new float[16];
        private final float[] mProjectionMatrix = new float[16];
        private final float[] mViewMatrix = new float[16];
        private final float[] mRotationMatrix = new float[16];

        // Source:
        // https://developer.android.com/training/graphics/opengl/draw.html
        private final String vertexShaderCode =
                // This matrix member variable provides a hook to manipulate
                // the coordinates of the objects that use this vertex shader
                "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                // The matrix must be included as a modifier of gl_Position.
                // Note that the uMVPMatrix factor *must be first* in order
                // for the matrix multiplication product to be correct.
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}";

        private final String fragmentShaderCode =
                "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}";

        private FloatBuffer vertexBuffer;
        private ShortBuffer indexBuffer;
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
            Matrix.frustumM(mProjectionMatrix, 0, -ratio * 2, ratio * 2, -2, 2, 3, 7);
        }

        @Override
        public void onDrawFrame(GL10 unused) {
            GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

            GLES20.glUseProgram(shaderProgram);

            int positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");

            GLES20.glVertexAttribPointer(positionHandle, 3,
                    GLES20.GL_FLOAT, false,
                    3 * 4, vertexBuffer);

            int colorHandle = GLES20.glGetUniformLocation(shaderProgram, "vColor");

            GLES20.glUniform4fv(colorHandle, 1, color, 0);

            int mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");

            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

            GLES20.glEnableVertexAttribArray(positionHandle);
            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES, index.length,
                    GLES20.GL_UNSIGNED_SHORT, indexBuffer);

            GLES20.glDisableVertexAttribArray(positionHandle);
        }
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
