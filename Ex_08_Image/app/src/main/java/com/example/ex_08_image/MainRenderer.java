package com.example.ex_08_image;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.google.ar.core.Session;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    CameraPreView mCamera;
    ObjRenderer mObj;
    List<ObjRenderer> mObjs = new ArrayList<>();


    boolean mViewportChanged, isImgFind = false;
    int mViewportWidth, mViewportHeight;
    RenderCallback mRenderCallback;

    MainRenderer(Context context, RenderCallback callback){
        mRenderCallback = callback;
        mCamera = new  CameraPreView();
        mObj = new ObjRenderer(context, "earth.obj", "earth.png");
        mObjs.add(new ObjRenderer(context, "earth.obj", "earth.png"));
        mObjs.add(new ObjRenderer(context, "moon.obj", "moon.png"));
    }

    interface RenderCallback{
        void preRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(1.0f,1.0f, 0.0f, 1.0f);

        mCamera.init();
        mObj.init();
        for (int i = 0; i < mObjs.size(); i++){
            mObjs.get(i).init();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0,0,width, height);
        mViewportChanged = true;
        mViewportWidth = width;
        mViewportHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mRenderCallback.preRender();

        GLES20.glDepthMask(false);
        mCamera.draw();
        GLES20.glDepthMask(true);
        if (isImgFind) {
            for (int i = 0; i < mObjs.size(); i++){
                mObjs.get(i).draw();
            }
            mObj.draw();
        }
    }
    void updateSession(Session session, int displayRotation){
        if(mViewportChanged){
            session.setDisplayGeometry(displayRotation, mViewportWidth, mViewportHeight);
            mViewportChanged = false;
        }
    }
    void setProjectionMatrix(float [] matrix){
        for (int i = 0; i < mObjs.size(); i++){
            mObjs.get(i).setProjectionMatrix(matrix);
        }

        mObj.setProjectionMatrix(matrix);

    }
    void updateViewMatrix(float [] matrix){
        for (int i = 0; i < mObjs.size(); i++){
            mObjs.get(i).setViewMatrix(matrix);
        }

        mObj.setViewMatrix(matrix);
    }

    int getTextureId(){
        return mCamera == null ? -1 : mCamera.mTextures[0];
    }


}