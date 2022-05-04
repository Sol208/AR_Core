package com.example.ex_07_plane;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.google.ar.core.Session;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    CameraPreView mCamera;
    PointCloudRenderer mPointCloud;
    PlaneRenderer mPlane;
    Cube mCube;
    PenguinRenderer mPenguin;
    JetRenderer mJet;
    boolean mViewportChanged;
    int mViewPortWidth, mViewPortHeight;
    RenderCallback mRenderCallback;

    MainRenderer(Context context, RenderCallback callBack){
        mRenderCallback = callBack;
        mCamera = new CameraPreView();
        mPointCloud = new PointCloudRenderer();
        mPlane = new PlaneRenderer(Color.BLUE, 0.7f);
        mCube = new Cube(0.3f, Color.CYAN, 0.8f);
        mPenguin = new PenguinRenderer(context, "Penguin.obj", "Penguin.png");
        mJet = new JetRenderer(context,"jet.obj", "jet.jpg");
    }

    interface RenderCallback {
        void preRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);

        mCamera.init();
        mPointCloud.init();
        mPlane.init();
        mCube.init();
        mPenguin.init();
        mJet.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mViewportChanged = true;
        mViewPortWidth = width;
        mViewPortHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mRenderCallback.preRender();
        GLES20.glDepthMask(false);
        mCamera.draw();
        GLES20.glDepthMask(true);
        mPointCloud.draw();
        mPlane.draw();
        mCube.draw();
        mPenguin.draw();
        mJet.draw();
    }

    void updateSession(Session session, int displayRotation){
        if (mViewportChanged){
            session.setDisplayGeometry(displayRotation, mViewPortWidth, mViewPortHeight);
            mViewportChanged = false;
        }
    }
    void setProjectionMatrix(float[] matrix){
        mPointCloud.updateProjMatrix(matrix);
        mPlane.setProjectionMatrix(matrix);
        mCube.setProjectionMatrix(matrix);
        mPenguin.setProjectionMatrix(matrix);
        mJet.setProjectionMatrix(matrix);
    }
    void updateViewMatrix(float[] matrix){
        mPointCloud.updateViewMatrix(matrix);
        mPlane.setViewMatrix(matrix);
        mCube.setViewMatrix(matrix);
        mPenguin.setViewMatrix(matrix);
        mJet.setViewMatrix(matrix);
    }

    int getTextureId(){
        return mCamera == null ? - 1 : mCamera.mTextures[0];
    }


}
