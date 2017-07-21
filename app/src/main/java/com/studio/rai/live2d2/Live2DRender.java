package com.studio.rai.live2d2;

import android.opengl.GLSurfaceView;

import com.studio.rai.live2d2.live2d.MyL2DModel;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by LUTAS on 2017/1/3.
 */

public class Live2DRender implements GLSurfaceView.Renderer
{
    private MyL2DModel mModelManager;

    public Live2DRender() {
        //mModelManagers = new ArrayList<>();
    }

    public void setModel(MyL2DModel modelManager) {
        mModelManager = modelManager;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mModelManager.onSurfaceCreated(gl);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mModelManager.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mModelManager.onDrawFrame(gl);
    }
}