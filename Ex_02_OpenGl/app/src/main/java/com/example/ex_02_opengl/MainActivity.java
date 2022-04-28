package com.example.ex_02_opengl;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    MyGLSurfaceView myView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //activity_main 이 아닌 myView를 메인으로 사용하겠다.
//        setContentView(R.layout.activity_main);
        myView = new MyGLSurfaceView(this);
        setContentView(myView);
    }

    @Override
    protected void onPause(){
        super.onPause();
        myView.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        myView.onResume();
    }

}