package com.example.ex_06_painting;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Line {
    // GPU를 이용하여 고속 계산 하여 화면 처리하기 위한 코드
    String vertexShaderCode =
            "attribute vec3 aPosition;" +
                    "attribute vec4 aColor;" +
                    "uniform mat4 uMVPMatrix;" +    // 4 x 4 형태의 상수로 지정
                    "varying vec4 vColor;" +
                    "void main (){" +
                    "vColor = aColor;" +
                    "gl_Position = uMVPMatrix * vec4(aPosition.x, aPosition.y, aPosition.z, 1.0);" +    // gl_Position : OpenGL에 있는 변수 ::> 계산식 uMVPMatrix * vPosition
                    "}";
    String fragmentShaderCode =
            "precision mediump float;" + //precision = 정밀도 mediump = 중간값
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "gl_FragColor = vColor;" +
                    "}";

    float[] mModelMatrix = new float[16];
    float[] mViewMatrix = new float[16];
    float[] mProjMatrix = new float[16];

    float[] mColor = new float[] {1.0f, 1.0f, 1.0f, 1.0f};

    // 현재 점의 번호
    int mNumPoints = 0;

    // 최대 점 갯수(점의 배열 좌표 갯수와 동일)
    int maxPoints = 1000;

    // 1000개의 점 * xyz
    float[] mPoint = new float[] {maxPoints * 3};

    FloatBuffer mVertices;
    FloatBuffer mColors;
    ShortBuffer mIndices;
    int mProgram;

    boolean isInited = false;

    int[] mVbo;

    // 새로운 라인 만들기
    Line() {    }

    // 그리기 직전에 좌표 수정
     void update() {

        short [] indices = {0, 1};

        // 점
        mVertices = ByteBuffer.allocateDirect(mPoint.length * Float.SIZE / 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(mPoint);
        mVertices.position(0);

         GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo[0]);
         GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mNumPoints * 3 * Float.BYTES, null, GLES20.GL_DYNAMIC_DRAW);
         GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


        // 색
        mColors = ByteBuffer.allocateDirect(mColor.length * Float.SIZE / 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mColors.put(mColor);
        mColors.position(0);

        // 순서
        mIndices = ByteBuffer.allocateDirect(indices.length * Float.SIZE / 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndices.put(indices);
        mIndices.position(0);

    }

    void updatePoint(float x, float y, float z){

        // 현재 점 번호의 좌표를 받는다.
        mPoint[mNumPoints * 3 + 0] = x;
        mPoint[mNumPoints * 3 + 1] = y;
        mPoint[mNumPoints * 3 + 2] = z;

        mNumPoints++; // 현재 점의 번호 증가
    }

    // 초기화
    void init(){

        mVbo = new int[1];
        GLES20.glGenBuffers(1, mVbo, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVbo[0]);

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, maxPoints * 3 * Float.BYTES, null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // 점위치 계산식
        int vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vShader, vertexShaderCode);
        GLES20.glCompileShader(vShader);

        // 텍스처
        int fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fShader, fragmentShaderCode);
        GLES20.glCompileShader(fShader);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vShader);
        GLES20.glAttachShader(mProgram, fShader);
        GLES20.glLinkProgram(mProgram);

        isInited = true;
    }


    // 도형 그리기 --> MyGLRenderer.onDrawFrame() 에서 호출하여 그리기
    void draw(float seekLine) {
        GLES20.glUseProgram(mProgram);

        // 점, 색 계산방식
        int position = GLES20.glGetAttribLocation(mProgram, "aPosition");
        int color = GLES20.glGetAttribLocation(mProgram, "aColor");
        int mvp = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] mvpMatrix = new float[16];  // view
        float[] mvMatrix = new float[16];   // projection



        Matrix.multiplyMM(mvMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mProjMatrix, 0, mvMatrix, 0);

        // mvp 번호에 해당하는 변수에 mvpMatrix 대입
        GLES20.glUniformMatrix4fv(mvp, 1, false, mvpMatrix, 0);

        // 점, 색 번호에 해당하는 변수에 각각 대입
        // 점 float * 점 3(삼각형)
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 4 * 3, mVertices);
        // 점 float * rgba
        GLES20.glVertexAttribPointer(color, 3, GLES20.GL_FLOAT, false, 4 * 4, mColors);


        // GPU 활성화
        GLES20.glEnableVertexAttribArray(position);
        GLES20.glEnableVertexAttribArray(color);
        GLES20.glLineWidth(50f); // 라인 두께

        // 그린다.
        //                        선으로 그린다.,
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, mNumPoints);


        // GPU비활성화
        GLES20.glDisableVertexAttribArray(position);
    }

    void setmModelMatrix(float[] matrix){
        System.arraycopy(matrix, 0, mModelMatrix, 0, 16);
    }

    void updateViewMatrix(float[] mViewMatrix) {
        // 배열 복제
        //               원본        시작위치   복사될 배열    복사배열 시작위치      개수
        System.arraycopy(mViewMatrix, 0, this.mViewMatrix, 0, 16);
    }

    void updateProjMatrix(float[] mProjMatrix) {
        // 배열 복제
        //               원본        시작위치   복사될 배열    복사배열 시작위치      개수
        System.arraycopy(mProjMatrix, 0, this.mProjMatrix, 0, 16);
    }
}