package com.studio.rai.live2d2.live2d;


import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by LUTAS on 2017/1/4.
 */

public class L2DModelSetting
{
    private static final String Tag = L2DModelSetting.class.getSimpleName();

    private String modelName;
    private String model;
    private String[] texures;
    private Map<String,String> expressions;
    private Map<String,String> motions;
    private Map<String,String> sounds;
    private String physics;

    public L2DModelSetting() {
        expressions = new LinkedHashMap<>();
        motions = new LinkedHashMap<>();
        sounds = new LinkedHashMap<>();
    }

    public L2DModelSetting(Context context, String modelName) throws JSONException, IOException {
        this();
        this.modelName = modelName;

        String jsonString = readFromFile(context, modelName);
        JSONObject jsonObject = new JSONObject(jsonString);

        initValue(jsonObject);
    }

    private String readFromFile(Context context, String modelName) throws IOException {
        String ret = "";

        String modelPath = modelName+ "/" + modelName+ ".model.json";
        InputStream inputStream = context.getAssets().open(modelPath);

        if ( inputStream != null ) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }

            inputStream.close();
            ret = stringBuilder.toString();
        }

        return ret;
    }

    private void initValue(JSONObject jsonObject) throws JSONException {
        initModel(jsonObject);
        initTexures(jsonObject);
        initMotions(jsonObject);
        initPhysics(jsonObject);
    }

    private void initModel(JSONObject jsonObject) throws JSONException {
        model = modelName + "/" + jsonObject.getString("model");
    }

    private void initTexures(JSONObject jsonObject) throws JSONException {
        JSONArray array = jsonObject.getJSONArray("textures");
        texures = jsonAry2StringAry(array);
    }

    private void initMotions(JSONObject jsonObject) throws JSONException {
        JSONObject motions = jsonObject.getJSONObject("motions");

        Iterator<String> iterator = motions.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            JSONArray motionGroup = motions.getJSONArray(key);
            for (int j=0;j<motionGroup.length();j++) {
                JSONObject object = motionGroup.getJSONObject(j);
                String motionPath = modelName + "/" +object.getString("file");
                this.motions.put(key+j, motionPath);
                if (!object.has("sound"))
                    continue;
                String soundPath = modelName + "/" +object.getString("sound");
                this.sounds.put(key+j, soundPath);
            }
        }
    }

    private void initPhysics(JSONObject jsonObject) throws JSONException {
        physics = modelName + "/" +jsonObject.getString("physics");
    }

    private String[] jsonAry2StringAry(JSONArray jsonArray) throws JSONException {
        String[] array = new String[jsonArray.length()];
        for (int i=0;i<array.length;i++) {
            array[i] = modelName + "/" +jsonArray.getString(i);
        }
        return array;
    }

    //========================== Getter & Setter ===================================================

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String[] getTexures() {
        return texures;
    }

    public void setTexures(String[] texures) {
        this.texures = texures;
    }

    public Map<String, String> getExpressions() {
        return expressions;
    }

    public void setExpressions(Map<String, String> expressions) {
        this.expressions = expressions;
    }

    public Map<String, String> getMotions() {
        return motions;
    }

    public void setMotions(Map<String, String> motions) {
        this.motions = motions;
    }

    public Map<String, String> getSounds() {
        return sounds;
    }

    public void setSounds(Map<String, String> sounds) {
        this.sounds = sounds;
    }

    public String getPhysics() {
        return physics;
    }

    public void setPhysics(String physics) {
        this.physics = physics;
    }
}
