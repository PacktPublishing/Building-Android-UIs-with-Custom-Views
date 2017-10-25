package com.packt.rrafols.draw;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.AttributeSet;

import com.packt.rrafols.draw.dataobject.Scene;
import com.packt.rrafols.draw.obj.WavefrontObjParser;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLDrawer extends GLSurfaceView implements WavefrontObjParser.ParserListener {
    private static final String TAG = GLDrawer.class.toString();

    private GLRenderer glRenderer;
    private Scene scene;

    public GLDrawer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setEGLContextClientVersion(2);

        glRenderer = new GLRenderer();
        setRenderer(glRenderer);

        WavefrontObjParser.parse(context, "suzanne.obj", this);
    }

    class GLRenderer implements GLSurfaceView.Renderer {
        private final float[] mMVPMatrix = new float[16];
        private final float[] mProjectionMatrix = new float[16];
        private final float[] mViewMatrix = new float[16];

        // Source (updated with color interpolation)
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


        private int shaderProgram;
        private float angle = 0.f;
        private long startTime;

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            initShaders();
            startTime = SystemClock.elapsedRealtime();
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
            perspectiveFrustrum(mProjectionMatrix, 45.f, ratio, 1.f, 300.f);
        }

        @Override
        public void onDrawFrame(GL10 unused) {
            angle = ((float) SystemClock.elapsedRealtime() - startTime) * 0.02f;
            GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            if (scene != null) {
                Matrix.setLookAtM(mViewMatrix, 0,
                        0, 0, -4,
                        0f, 0f, 0f,
                        0f, 1.0f, 0.0f);

                Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
                Matrix.rotateM(mMVPMatrix, 0, angle, 0.8f, 2.f, 1.f);

                GLES20.glUseProgram(shaderProgram);

                int mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
                GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

                scene.render(shaderProgram, "vPosition", "aColor");
            }
        }
    }

    // source: http://nehe.gamedev.net/article/replacement_for_gluperspective/21002/
    private static void perspectiveFrustrum(float[] matrix, float fov, float aspect, float zNear, float zFar) {
        float fH = (float) (Math.tan( fov / 360.0 * Math.PI ) * zNear);
        float fW = fH * aspect;

        Matrix.frustumM(matrix, 0, -fW, fW, -fH, fH, zNear, zFar);
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

    @Override
    public void parsingSuccess(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void parsingError(String message) {
        System.err.println(message);
    }
}
