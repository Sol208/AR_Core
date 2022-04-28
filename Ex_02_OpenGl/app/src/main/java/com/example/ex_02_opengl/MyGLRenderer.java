package com.example.ex_02_opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

//    Square myBox1, myBox2, myBox3, myBox4, myBox5, myBox6;
//
//    List<Square> squares = new ArrayList<>();
//
//    Star myStar;
//
//    float[] boxCoord1 = {
//            -0.2f, 0.7f, -1.0f,
//            -0.7f, 0.2f, -1.0f,
//            -0.2f, -0.3f, -1.0f,
//            0.3f, 0.2f, -1.0f
//    };
//
//    float[] boxCoord2 = {
//            -0.2f, 0.7f, 0.0f,
//            -0.7f, 0.2f, 0.0f,
//            -0.2f, -0.3f, 0.0f,
//            0.3f, 0.2f, 0.0f
//    };
//
//    float[] boxCoord3 = {
//            -0.2f, 0.7f, -1.0f,
//            -0.2f, 0.7f, 0.0f,
//            -0.7f, 0.2f, 0.0f,
//            -0.7f, 0.2f, -1.0f
//    };
//
//    float[] boxCoord4 = {
//            -0.2f, 0.7f, -1.0f,
//            -0.2f, 0.7f, 0.0f,
//            0.3f, 0.2f, 0.0f,
//            0.3f, 0.2f, -1.0f
//    };
//
//    float[] boxCoord5 = {
//            -0.7f, 0.2f, -1.0f,
//            -0.7f, 0.2f, 0.0f,
//            -0.2f, -0.3f, 0.0f,
//            -0.2f, -0.3f, -1.0f
//    };
//
//    float[] boxCoord6 = {
//            0.3f, 0.2f, -1.0f,
//            0.3f, 0.2f, 0.0f,
//            -0.2f, -0.3f, 0.0f,
//            -0.2f, -0.3f, -1.0f
//    };
//
//    float[] boxColor1 = {
//            1.0f, 0.5f, 0.3f, 1.0f
//    };
//
//    float[] boxColor2 = {
//            0.5f, 1.0f, 0.0f, 1.0f
//    };
//
//    float[] boxColor3 = {
//            0.5f, 0.5f, 0.0f, 1.0f
//    };
//
//    float[] boxColor4 = {
//            0.5f, 0.5f, 1.0f, 1.0f
//    };
//
//    float[] boxColor5 = {
//            0.0f, 0.5f, 1.0f, 1.0f
//    };
//
//    float[] boxColor6 = {
//            1.0f, 0.0f, 1.0f, 1.0f
//    };
//
//    short[] boxOrder = {
//            0, 1, 2, 0, 2, 3
//    };

    ObjRenderer myTable;

    float[] mMVPMatrix = new float[16];

    float[] mProjectionMatrix = new float[16];

    float[] mViewMatrix = new float[16];

    MyGLRenderer(Context context) {
        myTable = new ObjRenderer(context, "table.obj", "table.jpg");
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
//        myBox1 = new Square(boxCoord1, boxColor1, boxOrder);
//        myBox2 = new Square(boxCoord2, boxColor2, boxOrder);
//        myBox3 = new Square(boxCoord3, boxColor3, boxOrder);
//        myBox4 = new Square(boxCoord4, boxColor4, boxOrder);
//        myBox5 = new Square(boxCoord5, boxColor5, boxOrder);
//        myBox6 = new Square(boxCoord6, boxColor6, boxOrder);
//        squares.add(myBox1);
//        squares.add(myBox3);
//        squares.add(myBox4);
//        squares.add(myBox5);
//        squares.add(myBox6);
//        squares.add(myBox2);
        myTable.init();
    }


    // 화면 갱신 되면서 화면에서 배치
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width*30 / height; // 비율


        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -100, 100, 20, 100);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // mViewMatrix, 배열의 어디서 부터, position, center, up
        Matrix.setLookAtM(mViewMatrix, 0,
                //기본값 x, y, z
                0, 0, -30, // 카메라 위치
                0, 0, -30, // 카메라 시선
                0, 1, 0 // 카메라 윗방향
        );
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        Matrix.setIdentityM(mMVPMatrix,0);
        myTable.setProjectionMatrix(mProjectionMatrix);
        myTable.setViewMatrix(mViewMatrix);
        myTable.setModelMatrix(mMVPMatrix);

        // 정사각형 그리기
        myTable.draw();
//        myStar.draw(mMVPMatrix);
//        for (int i = 0; i < squares.size(); i++) {
//            squares.get(i).draw(mMVPMatrix);
//        }
    }

    // GPU를 이용하여 그리기를 연산한다.
    static int loadShader(int type, String shaderCode) {
        int res = GLES20.glCreateShader(type);

        GLES20.glShaderSource(res, shaderCode);
        GLES20.glCompileShader(res);

        return res;
    }

}
