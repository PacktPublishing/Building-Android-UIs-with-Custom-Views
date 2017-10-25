package com.packt.rrafols.draw;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.AttributeSet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLDrawerES1 extends GLSurfaceView {
    private GLRenderer glRenderer;

    public GLDrawerES1(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        glRenderer = new GLRenderer();
        setRenderer(glRenderer);
    }
}

class GLRenderer implements GLSurfaceView.Renderer {
    private static float quadCoords[] = {
            -1.f, -1.f, 0.f,
            -1.f,  1.f, 0.f,
             1.f,  1.f, 0.f,
             1.f, -1.f, 0.f};

    private static short[] indices = {
            0, 1, 2,
            0, 2, 3
    };

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;

    GLRenderer() {
        ByteBuffer vbb = ByteBuffer.allocateDirect(quadCoords.length * 4);
        vbb.order(ByteOrder.nativeOrder());

        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(quadCoords);
        vertexBuffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
        ibb.order(ByteOrder.nativeOrder());

        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (height == 0) height = 1;
        float aspect = (float) width / height;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(-10.f, 10.f, 10.f / aspect, -10.f / aspect, 0.1f, 100.f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClearColor(1.f, 0.f, 0.f, 1.f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        gl.glLoadIdentity();
        gl.glTranslatef(0.f, 0.f, -50.f);

        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);
        gl.glRotatef(angle, 0.f, 0.f, 1.f);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glColor4f(1.f, 1.f, 1.f, 1.f);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_SHORT, indexBuffer);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
}
