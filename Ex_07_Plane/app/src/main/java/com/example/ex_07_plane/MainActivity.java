package com.example.ex_07_plane;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
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
import com.google.ar.core.LightEstimate;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView myTextView;
    GLSurfaceView mSurfaceView;
    MainRenderer mRenderer;

    Session mSession;
    Config mConfig;
    Button redBtn, greenBtn, blueBtn, purpleBtn, yellowBtn;

    boolean mUserRequestInstall = true, mTouched = false;
    float mCurrentX, mCurrentY;
    float[] colorSet = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
    float lightIntensity = 1.0f;

    float[] modelMatrix;
    float[] jetMatrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBarAndTitleBar();
        setContentView(R.layout.activity_main);

        mSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);
        myTextView = (TextView) findViewById(R.id.myTextView);
        redBtn = (Button) findViewById(R.id.redBtn);
        greenBtn = (Button) findViewById(R.id.greenBtn);
        blueBtn = (Button) findViewById(R.id.blueBtn);
        purpleBtn = (Button) findViewById(R.id.purpleBtn);
        yellowBtn = (Button) findViewById(R.id.yellowBtn);

        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if (displayManager != null) {
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int i) {
                }

                @Override
                public void onDisplayRemoved(int i) {
                }

                @Override
                public void onDisplayChanged(int i) {
                    synchronized (this) {
                        mRenderer.mViewportChanged = true;
                    }
                }
            }, null);
        }
        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRenderer.mPenguin.setLightIntensity(progress / 100.0f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        redBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = ((ColorDrawable) redBtn.getBackground()).getColor();
                colorSet = new float[]{
                        Color.red(color) / 255f,
                        Color.green(color) / 255f,
                        Color.blue(color) / 255f,
                        1.0f
                };
                mRenderer.mPenguin.setColorCorrection(colorSet);
            }
        });

        greenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = ((ColorDrawable) greenBtn.getBackground()).getColor();
                colorSet = new float[]{
                        Color.red(color) / 255f,
                        Color.green(color) / 255f,
                        Color.blue(color) / 255f,
                        1.0f
                };
                mRenderer.mPenguin.setColorCorrection(colorSet);
            }
        });

        blueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = ((ColorDrawable) blueBtn.getBackground()).getColor();
                colorSet = new float[]{
                        Color.red(color) / 255f,
                        Color.green(color) / 255f,
                        Color.blue(color) / 255f,
                        1.0f
                };
                mRenderer.mPenguin.setColorCorrection(colorSet);
            }
        });

        purpleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = ((ColorDrawable) purpleBtn.getBackground()).getColor();
                colorSet = new float[]{
                        Color.red(color) / 255f,
                        Color.green(color) / 255f,
                        Color.blue(color) / 255f,
                        1.0f
                };
                mRenderer.mPenguin.setColorCorrection(colorSet);
            }
        });

        yellowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = ((ColorDrawable) yellowBtn.getBackground()).getColor();
                colorSet = new float[]{
                        Color.red(color) / 255f,
                        Color.green(color) / 255f,
                        Color.blue(color) / 255f,
                        1.0f
                };
                mRenderer.mPenguin.setColorCorrection(colorSet);
            }
        });

        mRenderer = new MainRenderer(this, new MainRenderer.RenderCallback() {
            @Override
            public void preRender() {
                if (mRenderer.mViewportChanged) {
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

                if (frame.hasDisplayGeometryChanged()) {
                    mRenderer.mCamera.transformDisplayGeometry(frame);
                }

                PointCloud pointCloud = frame.acquirePointCloud();
                mRenderer.mPointCloud.update(pointCloud);
                pointCloud.release();

                // ?????? ?????????
                if (mTouched) {
                    LightEstimate estimate = frame.getLightEstimate();
                    // LightEstimate :: ?????? ?????? ????????? ????????? ?????????
                    // getPixelIntensity(); ?????? ?????? ?????? (0 ~ 1.0)

                    lightIntensity = estimate.getPixelIntensity();

                    // ?????? ??????
                    List<HitResult> results = frame.hitTest(mCurrentX, mCurrentY);

                    float[] colorCorrection = new float[4];

                    // ?????? ?????? ????????????
                    estimate.getColorCorrection(colorCorrection, 0);

                    for (HitResult result : results) {
                        Pose pose = result.getHitPose(); // ????????????????????? ??????
                        modelMatrix = new float[16];
                        jetMatrix = new float[16];
                        pose.toMatrix(modelMatrix, 0); // ????????? ????????? matrix??? ???
                        pose.toMatrix(jetMatrix,0);

                        // ??????????????? ????????? ????????? ????????? ????????????.(Plane??? ?????? ?????????)
                        Trackable trackable = result.getTrackable();

                        // ????????????(??????)
                        Matrix.scaleM(modelMatrix, 0, 0.03f, 0.03f, 0.03f);
                        Matrix.scaleM(jetMatrix, 0, 0.008f, 0.008f, 0.008f);

                        // ??????
                        //                               ??????    ??????    ???(0?????? ??????, ????????? ??????)
                        Matrix.rotateM(modelMatrix, 0, 90, 1f, 0f, 0f);
                        Matrix.rotateM(jetMatrix, 0, 0,0f, 34f, 0f);

                        // ????????? ?????? ????????? Plane ?????? ???????????? if???
                        if (trackable instanceof Plane &&
                                ((Plane) trackable).isPoseInPolygon(pose)) { // Plane ?????????(???)??? ?????? ????????? ????????? ??????

                            // ?????? ???????????? ?????????.
                            mRenderer.mPenguin.setLightIntensity(lightIntensity);
                            // ???????????????????????? ?????? ?????? magenta??? ????????? ??????
                            // mRenderer.mObj.setColorCorrection(new float[]{1.0f, 0.0f, 1.0f,1.0f});
                            mRenderer.mPenguin.setColorCorrection(colorSet);
                            mRenderer.mJet.setColorCorrection(colorSet);
                            // ????????? modelMatrix??? ????????? ???????????? modelMatrix??? ??????
//                            mRenderer.mCube.setModelMatrix(cubeMatrix);
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    for (float i = (float) 1; i <= (float) 30; i++) {
                                        // ??????
                                        Matrix.translateM(modelMatrix, 0, 0, i / 10, 0f);
                                        mRenderer.mPenguin.setModelMatrix(modelMatrix);
                                        Matrix.translateM(jetMatrix, 0, 0f, 0f, (i / 10)*2);
                                        mRenderer.mJet.setModelMatrix(jetMatrix);

                                        SystemClock.sleep(50);
                                    }
                                }
                            }.start();
                        }
                    }

                    mTouched = false;
                }

                // ?????? ?????? ????????? ?????????
                // Session???????????? ???????????? ???????????? "??????"?????? "???" ????????? ?????? ??? ??????.
                //                                    Plane     Point
                Collection<Plane> planes = mSession.getAllTrackables(Plane.class); // ARCore ?????? Plane?????? ?????????.

                boolean isPlaneDetected = false;

                //plane??? ???????????????
                for (Plane plane : planes) {
                    if (plane.getTrackingState() == TrackingState.TRACKING &&
                            plane.getSubsumedBy() == null) {
                        isPlaneDetected = true;
                        // ??????????????? plane ????????? ???????????? ??????
                        mRenderer.mPlane.update(plane);
                    }
                }

                if (isPlaneDetected) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myTextView.setText("????????? ????????????!");
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myTextView.setText("????????? ????????? ???????");
                        }
                    });
                }

                Camera camera = frame.getCamera();
                float[] projMatrix = new float[16];
                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100f);

                float[] viewMatrix = new float[16];
                camera.getViewMatrix(viewMatrix, 0);

                mRenderer.setProjectionMatrix(projMatrix);
                mRenderer.updateViewMatrix(viewMatrix);
            }
        });

        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mSurfaceView.setRenderer(mRenderer);


    }


    @Override
    protected void onResume() {
        super.onResume();
        requestCameraPermission();
        try {
            if (mSession == null) {
                switch (ArCoreApk.getInstance().requestInstall(this, true)) {
                    case INSTALLED:
                        mSession = new Session(this);
                        Log.d("??????", " ARCore session ??????");
                        break;

                    case INSTALL_REQUESTED:
                        Log.d("??????", " ARCore ?????? ??????");
                        mUserRequestInstall = false;
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mConfig = new Config(mSession);

        mSession.configure(mConfig);

        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }

        mSurfaceView.onResume();
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSurfaceView.onPause();
        mSession.pause();
    }

    private void hideStatusBarAndTitleBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
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
            mCurrentX = event.getX();
            mCurrentY = event.getY();

        }
        return true;
    }

//    public void btnClick(View view){
//        int color = ((ColorDrawable)view.getBackground()).getColor();
//        float[] colorSet = new float[]{
//                Color.red(color)/255f,
//                Color.green(color)/255f,
//                Color.blue(color)/255f,
//                1.0f
//        };
//        mRenderer.mObj.setColorCorrection(colorSet);
//    }


}