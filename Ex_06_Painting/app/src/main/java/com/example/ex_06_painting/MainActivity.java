package com.example.ex_06_painting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    Session mSession;
    GLSurfaceView mySurView;
    MainRenderer mRenderer;
    Config mConfig;    // ARCore Session 설정정보를 받을 변수
    Float displayX, displayY;

    //       새로운 선인지,      점추가인지
    Boolean mNewPath = false, mPointAdd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //타이틀 바 없애기
        hideStatusBar();

        setContentView(R.layout.activity_main);
        mySurView = (GLSurfaceView) findViewById(R.id.glsurfaceview);

        //MainAtivity의 화면 관리 매니저 --> 화면변화를 감지 :: 현재 시스템에서 서비스 지원
        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);

        //화면 변화가 발생되면 MainRenderer의 화면 변환을 실행시킨다.
        if(displayManager != null){
            //화면에 대한 리스너 실행
            displayManager.registerDisplayListener(
                    new DisplayManager.DisplayListener() {
                        @Override
                        public void onDisplayAdded(int displayId) {
                        }
                        @Override
                        public void onDisplayRemoved(int displayId) {
                        }
                        @Override
                        public void onDisplayChanged(int displayId) {
                            synchronized (this) {
                                //화면 갱신 인지 메소드 실행
                                mRenderer.onDisplayChanged();
                            }
                        }
                    },
                    null
            );
        }

        MainRenderer.RendererCallBack mr = new MainRenderer.RendererCallBack() {

            @Override
            public void preRender() {
                //화면이 회전되었다면,
                if (mRenderer.viewportChanged){
                    //현재 화면 가져오기
                    Display display = getWindowManager().getDefaultDisplay();

                    mRenderer.updateSession(mSession, display.getRotation());
                }

                // session 객체와 연결해서 화면 그리기 하기
                mSession.setCameraTextureName(mRenderer.getTextureId());

                // 화면 그리기에서 사용할 frame --> session 이 업데이트 되면 새로운 프레임을 받는다.
                Frame frame = null;

                try {
                    frame = mSession.update();
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }
                // 화면(카메라 정보)을 바꾸기 위한 작업
                mRenderer.transformDisplayGeometry(frame);


                //// ↓↓↓↓↓↓↓ PointCloud 설정구간
                // ARCore 에 정의된 클래스로
                // 현재 프레임에서 특정있는 점들에 대한 포인트값(3차원 좌표값)
                PointCloud pointCloud = frame.acquirePointCloud();

                // 포인트 값을 적용시키기위해 MainRenderer -> PointCloud.update() 실행
                mRenderer.mPointCloud.update(pointCloud);

                // 사용이 끝난 포인트 자원해제
                pointCloud.release();


                /* 화면 터치시 작업 시작*/
                if (mNewPath){
                    mNewPath = false;
                    List<HitResult> arr = frame.hitTest(displayX, displayY);

                    for(HitResult hr : arr){
                        Pose pose = hr.getHitPose();

                        // 새로운 라인 그리기
                        mRenderer.addLine(pose.tx(), pose.ty(), pose.tz());
                        break;
                    }
                } else if (mPointAdd){
                    List<HitResult> arr = frame.hitTest(displayX, displayY);

                    for(HitResult hr : arr){
                        Pose pose = hr.getHitPose();

                        // 점 추가
                        mRenderer.addPoint(pose.tx(), pose.ty(), pose.tz());
                        break;
                    }
                    mPointAdd = false;
                }
                /* 화면 터치시 작업 끝*/

                // 카메라 frame 에서 받는다
                // --> mPointCloud 에서 렌더링 할 때 카메라의 좌표계산을  받아서 처리
                Camera camera = frame.getCamera();

                float[] projMatrix = new float[16];
                float[] viewMatrix = new float[16];

                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f);

                camera.getViewMatrix(viewMatrix, 0);

//                mRenderer.mPointCloud.updateMatrix(viewMatrix, projMatrix);
                mRenderer.updateViewMatrix(viewMatrix);
                mRenderer.updateProjMatrix(projMatrix);
            }
        };

        mRenderer = new MainRenderer(mr);
        // pause 시 관련 데이터가 사라지는 것을 막는다.
        mySurView.setPreserveEGLContextOnPause(true);
        mySurView.setEGLContextClientVersion(2); // 버전을 2.0 사용

        // 화면을 그리는 Renderer를 지정한다.
        // 새로 정의한 MainRenderer를 사용한다.
        mySurView.setRenderer(mRenderer);

        // 랜더링 계속 호출
        mySurView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mySurView.onPause();
        mSession.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraPerm();

        try {
            if (mSession == null){

                switch(ArCoreApk.getInstance().requestInstall(this,true)){
                    case INSTALLED : // ARCore 정상설치 됨
                        // ARCore 가 정상설치 되어서 session 을 생성가능한 형태임
                        mSession = new Session(this);
                        Log.d("Session=> ", "Session Create");
                        break;

                    case INSTALL_REQUESTED : // ARCore 설치필요
                        Log.d("Session=> ", "Session Install_Requested");
                        break;
                }


//
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //화면 갱신 시 Session정보 를 받아서 내Session의 설정으로 올린다.
        mConfig = new Config(mSession);

        mSession.configure(mConfig);

        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
        mySurView.onResume();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        displayX = event.getX();
        displayY = event.getY();
        // 맨  처음 찍기(선 그리기 시작)
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN :
                mNewPath = true; // 새로운 선
                mPointAdd = true;// 점 추가
                break;

            case MotionEvent.ACTION_MOVE :
                mPointAdd = true;// 점 추가
                break;

            case MotionEvent.ACTION_UP :
                mPointAdd = false;// 점 추가 완료
                break;
        }

//        Log.d("MainActivity : " , "Touch!!" + event.getX() + "," + event.getY());
        return true;
    }

    // 카메라 퍼미션 요청
    void cameraPerm(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},0);

        }
    }

    void hideStatusBar(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }

}
