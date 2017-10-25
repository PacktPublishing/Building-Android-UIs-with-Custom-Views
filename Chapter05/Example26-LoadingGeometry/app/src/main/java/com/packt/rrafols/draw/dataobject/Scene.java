package com.packt.rrafols.draw.dataobject;

import android.opengl.GLES20;

import java.util.ArrayList;

public class Scene {

    private ArrayList<Object3D> objects;

    public Scene() {
        objects = new ArrayList<>();
    }

    public void addObject(Object3D obj) {
        objects.add(obj);
    }

    public ArrayList<Object3D> getObjects() {
        return objects;
    }

    public void render(int shaderProgram, String posAttributeName, String colAttributeName) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        for (int i = 0; i < objects.size(); i++) {
            objects.get(i).render(shaderProgram, posAttributeName, colAttributeName);
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }
}
