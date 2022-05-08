package com.example.ex_08_image;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {

    GLSurfaceView mSurfaceView;
    MainRenderer mRenderer;

    Session mSession;
    Config mConfig;
    Bitmap bitmap;
    float i = 0.001f;

    boolean mUserRequestedInstall = true, mTouched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBarANdTitleBar();
        setContentView(R.layout.activity_main);

        mSurfaceView = (GLSurfaceView)findViewById(R.id.gl_surface_view);

        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if(displayManager != null){
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int i) {}

                @Override
                public void onDisplayRemoved(int i) {}

                @Override
                public void onDisplayChanged(int i) {
                    synchronized (this){
                        mRenderer.mViewportChanged = true;
                    }
                }
            }, null);
        }

        mRenderer = new MainRenderer(this,new MainRenderer.RenderCallback() {
            @Override
            public void preRender() {

                if(mRenderer.mViewportChanged){
                    Display display = getWindowManager().getDefaultDisplay();
                    int displayRotation = display.getRotation();
                    mRenderer.updateSession(mSession, displayRotation);
                }

                mSession.setCameraTextureName(mRenderer.getTextureId());

                Frame frame = null;

                try {
                    frame = mSession.update();
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }

                if(frame.hasDisplayGeometryChanged()){
                    mRenderer.mCamera.transformDisplayGeometry(frame);
                }

                Camera camera = frame.getCamera();
                float [] projMatrix = new float[16];
                camera.getProjectionMatrix(projMatrix,0,0.1f, 100f);
                float [] viewMatrix = new float[16];
                camera.getViewMatrix(viewMatrix,0);

                drawImages(frame);

                mRenderer.updateViewMatrix(viewMatrix);
                mRenderer.setProjectionMatrix(projMatrix);
            }
        });


        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setEGLConfigChooser(8,8,8,8,16,0);
        mSurfaceView.setRenderer(mRenderer);

    }

    @Override
    protected void onResume() {
        super.onResume();
        requestCameraPermission();
        try {
            if(mSession==null){
                switch(ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)){
                    case INSTALLED:
                        mSession = new Session(this);
                        Log.d("메인"," ARCore session 생성");
                        break;
                    case INSTALL_REQUESTED:
                        Log.d("메인"," ARCore 설치가 필요함");
                        mUserRequestedInstall = false;
                        break;

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        mConfig = new Config(mSession);

        mConfig.setFocusMode(Config.FocusMode.AUTO);

        // 이미지데이터베이스 설정
        setUpImgDB(mConfig);

        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }

        // 이미지데이터베이스 설정 후 세션에 적용
        mSession.configure(mConfig);

        mSurfaceView.onResume();
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    // 이미지데이터베이스 설정
    void setUpImgDB(Config config){
        // 이미지데이터베이스 생성
        AugmentedImageDatabase imageDatabase = new AugmentedImageDatabase(mSession);

        try {

            // 파일스트림 로드
            InputStream is = getAssets().open("sola_system.png");
            // 파일스트림에서 Bitmap 생성
            bitmap = BitmapFactory.decodeStream(is);
            // 이미지데이터베이스에 bitmap 추가
            imageDatabase.addImage("sola_system", bitmap);
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // session config에 생성한 이미지데이터베이스로 설정
        // 이미지추적 활성화
        config.setAugmentedImageDatabase(imageDatabase);

    }

    // 이미지 추적 결과에 따른 그리기 설정
    void drawImages(Frame frame){
//        mRenderer.isImgFind = false;

        // frame(카메라) 에서 찾은 이미지들을 Collection으로 받아온다.
        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);

        // 찾은 이미지들을 돌린다.
//        if (mTouched) {
        for (AugmentedImage img : updatedAugmentedImages) {
            if (img.getTrackingState() == TrackingState.TRACKING) {
                mRenderer.isImgFind = true;
                Pose imgPose = img.getCenterPose();
                Log.d("Image Tracking Complete ==> ", img.getIndex() + "" + img.getName() +
                        "" + imgPose.tx() + "" + imgPose.ty() + "" + imgPose.tz());

                float[] earthMatrix = new float[16];
                float[] moonMatrix = new float[16];
                float[] jetMatrix = new float[16];
                imgPose.toMatrix(earthMatrix, 0);
                imgPose.toMatrix(moonMatrix, 0);
                imgPose.toMatrix(jetMatrix, 0);

                Matrix.translateM(earthMatrix, 0, 0.1f, 0.0f, 0.0f);
                Matrix.rotateM(earthMatrix, 0, 0, 1.0f, 0.0f, 0.0f);
                Matrix.scaleM(earthMatrix, 0, 0.03f, 0.03f, 0.03f);

                Matrix.translateM(moonMatrix, 0, -0.1f, 0.0f, 0.0f);
                Matrix.rotateM(moonMatrix, 0, 0, 1.0f, 0.0f, 0.0f);
                Matrix.scaleM(moonMatrix, 0, 0.03f, 0.03f, 0.03f);

                mRenderer.mObjs.get(0).setModelMatrix(earthMatrix);
                mRenderer.mObjs.get(1).setModelMatrix(moonMatrix);

                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        if (i < 0.03f) {
                            BigDecimal iNum = new BigDecimal(i+"");
                            BigDecimal nNum = new BigDecimal("0.001");
                            Log.d("i Num = ", i + "");
                            Matrix.scaleM(jetMatrix, 0, i, i, i);
                            Matrix.translateM(jetMatrix, 0, 0.0f, i*1000, 0.0f);
                            Matrix.rotateM(jetMatrix, 0, -90, 1f, 0.0f, 0.0f);
                            mRenderer.mObjs.get(2).setModelMatrix(jetMatrix);
                            SystemClock.sleep(100);
                            i = iNum.add(nNum).floatValue();;
                        }
                    }
                }.start();
            }
//                mRenderer.mObj.setModelMatrix(earthMatrix);


                // Use getTrackingMethod() to determine whether the image is currently
                // being tracked by the camera.
//                switch (img.getTrackingMethod()) {
//                    case LAST_KNOWN_POSE:
//                        // The planar target is currently being tracked based on its last
//                        // known pose.
//                        break;
//                    case FULL_TRACKING:
//                        // The planar target is being tracked using the current camera image.
//                        break;
//                    case NOT_TRACKING:
//                        // The planar target isn't been tracked.
//                        break;
//            }
            }
//            mTouched = false;
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
        mSession.pause();
    }

    void hideStatusBarANdTitleBar(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }

    void requestCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    0
            );
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mTouched = true;
        }
        return true;
    }
}