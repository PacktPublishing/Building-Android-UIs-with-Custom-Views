package com.packt.rrafols.draw.obj;

import android.content.Context;

import com.packt.rrafols.draw.dataobject.Object3D;
import com.packt.rrafols.draw.dataobject.Scene;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WavefrontObjParser {

    public static void parse(Context context, String name, ParserListener listener) {
        WavefrontObjParserHelper helper = new WavefrontObjParserHelper(context, name, listener);
        helper.start();
    }

    public interface ParserListener {
        void parsingSuccess(Scene scene);
        void parsingError(String message);
    }
}

class WavefrontObjParserHelper extends Thread {
    private String name;
    private WavefrontObjParser.ParserListener listener;
    private Context context;

    WavefrontObjParserHelper(Context context, String name, WavefrontObjParser.ParserListener listener) {
        this.context = context;
        this.name = name;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {

            InputStream is = context.getAssets().open(name);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            Scene scene = new Scene();
            Object3D obj = null;

            String str;
            while ((str = br.readLine()) != null) {
                if (!str.startsWith("#")) {
                    String[] line = str.split(" ");

                    if("o".equals(line[0])) {
                        if (obj != null) obj.prepare();
                        obj = new Object3D();
                        scene.addObject(obj);

                    } else if("v".equals(line[0])) {
                        float x = Float.parseFloat(line[1]);
                        float y = Float.parseFloat(line[2]);
                        float z = Float.parseFloat(line[3]);
                        obj.addCoordinate(x, y, z);
                    } else if("f".equals(line[0])) {

                        int a = getFaceIndex(line[1]);
                        int b = getFaceIndex(line[2]);
                        int c = getFaceIndex(line[3]);

                        if (line.length == 4) {
                            obj.addFace(a, b, c);
                        } else {
                            int d = getFaceIndex(line[4]);
                            obj.addFace(a, b, c, d);
                        }
                    } else {
                        // skip
                    }
                }
            }
            if (obj != null) obj.prepare();
            br.close();

            if (listener != null) listener.parsingSuccess(scene);
        } catch(Exception e) {
            if (listener != null) listener.parsingError(e.getMessage());
            e.printStackTrace();
        }
    }

    private static int getFaceIndex(String face) {
        if(!face.contains("/")) {
            return Integer.parseInt(face) - 1;
        } else {
            return Integer.parseInt(face.split("/")[0]) - 1;
        }
    }
}