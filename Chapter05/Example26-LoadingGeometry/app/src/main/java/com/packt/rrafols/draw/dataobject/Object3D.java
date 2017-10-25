package com.packt.rrafols.draw.dataobject;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

public class Object3D {
    private static final String TAG = Object3D.class.toString();

    private float[] translation;
    private float[] rotation;
    private float[] scale;

    private short[] indexes;
    private float[] coordinates;
    private float[] colors;

    private ArrayList<Float> coordinateList;
    private ArrayList<Short> indexList;

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer indexBuffer;

    public Object3D() {
        translation = new float[3];
        rotation = new float[3];
        scale = new float[3];

        coordinateList = new ArrayList<>();
        indexList = new ArrayList<>();
    }

    public void addCoordinate(float x, float y, float z) {
        coordinateList.add(x);
        coordinateList.add(y);
        coordinateList.add(z);
    }

    public void addFace(int a, int b, int c) {
        indexList.add((short) a);
        indexList.add((short) b);
        indexList.add((short) c);
    }

    public void addFace(int a, int b, int c, int d) {
        indexList.add((short) a);
        indexList.add((short) b);
        indexList.add((short) c);

        indexList.add((short) a);
        indexList.add((short) c);
        indexList.add((short) d);
    }

    public void prepare() {
        if (coordinateList.size() > 0 && coordinates == null) {
            coordinates = new float[coordinateList.size()];
            for (int i = 0; i < coordinateList.size(); i++) {
                coordinates[i] = coordinateList.get(i);
            }
        }

        if (indexList.size() > 0 && indexes == null) {
            indexes = new short[indexList.size()];
            for (int i = 0; i < indexList.size(); i++) {
                indexes[i] = indexList.get(i);
            }
        }

        colors = new float[(coordinates.length/3) * 4];
        for (int i = 0; i < colors.length/4; i++) {
            float intensity = (float) (Math.random() * 0.5 + 0.4);
            colors[i * 4    ] = intensity;
            colors[i * 4 + 1] = intensity;
            colors[i * 4 + 2] = intensity;
            colors[i * 4 + 3] = 1.f;
        }

        ByteBuffer vbb = ByteBuffer.allocateDirect(coordinates.length * (Float.SIZE / 8));
        vbb.order(ByteOrder.nativeOrder());

        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(coordinates);
        vertexBuffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(indexes.length * (Short.SIZE / 8));
        ibb.order(ByteOrder.nativeOrder());

        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indexes);
        indexBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * (Float.SIZE / 8));
        cbb.order(ByteOrder.nativeOrder());

        colorBuffer = cbb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        Log.i(TAG, "Loaded obj with " + coordinates.length + " vertices & " + (indexes.length/3) + " faces");
    }

    public void render(int shaderProgram, String posAttributeName, String colAttributeName) {
        int positionHandle = GLES20.glGetAttribLocation(shaderProgram, posAttributeName);
        GLES20.glVertexAttribPointer(positionHandle, 3,
                GLES20.GL_FLOAT, false,
                3 * 4, vertexBuffer);

        int colorHandle = GLES20.glGetAttribLocation(shaderProgram, colAttributeName);
        GLES20.glVertexAttribPointer(colorHandle, 4,
                GLES20.GL_FLOAT, false,
                4 * 4, colorBuffer);


        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, indexes.length,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
    }

    public void setTranslation(float[] translation) {
        this.translation = translation;
    }

    public void setRotation(float[] rotation) {
        this.rotation = rotation;
    }

    public void setScale(float[] scale) {
        this.scale = scale;
    }

    public void setCoordinates(float[] coordinates) {
        this.coordinates = coordinates;
    }

    public void setIndexes(short[] indexes) {
        this.indexes = indexes;
    }

    public static float[] stringArrayToFloat(String str) {
        String[] strArray = str.split(" ");
        float[] floatArray = new float[strArray.length];

        for(int i = 0; i < strArray.length; i++) {
            floatArray[i] = Float.parseFloat(strArray[i]);
        }

        return floatArray;
    }

    public static short[] stringArrayToShort(String str) {
        String[] strArray = str.split(" ");
        short[] shortArray = new short[strArray.length];

        for(int i = 0; i < strArray.length; i++) {
            shortArray[i] = Short.parseShort(strArray[i]);
        }

        return shortArray;
    }
}
