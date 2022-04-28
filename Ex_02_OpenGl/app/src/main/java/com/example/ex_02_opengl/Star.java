package com.example.ex_02_opengl;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Star {

    float[] coord, color;
    short[] order;

    // GPU를 이용하여 고속 계산 하여 화면 처리하기 위한 코드
    String vertexShaderCode =
                    "uniform mat4 uMVPMatrix;" +    // 4 x 4 형태의 상수로 지정
                    "attribute vec4 vPosition;" +    // vec4 -> 3차원 좌표
                    "void main (){" +
                    "gl_Position = uMVPMatrix * vPosition;" +    // gl_Position : OpenGL에 있는 변수 ::> 계산식 uMVPMatrix * vPosition
                    "}";
    String fragmentShaderCode =
                    "precision mediump float;" + //precision = 정밀도 mediump = 중간값
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "gl_FragColor = vColor;" +
                    "}";

    // 직사각형 점의 좌표
//    static float[] squareCoords = {
//            -0.25f, 0.7f, -0.5f,
//            -0.7f, 0.25f, -0.5f,
//            -0.25f, -0.3f, -0.5f,
//            0.3f, 0.25f, -0.5f
//    };

//    float[] color = {1.0f, 0.5f, 0.3f, 1.0f};

    // 그리는 순서
//    short[] drawOrder = {0,1,2,    0,2,3};

    FloatBuffer vertexBuffer;
    ShortBuffer drawBuffer;
    int mProgram;


    public Star(float[] coord, float[] color, short[] order){
        this.coord = coord;
        this.color = color;
        this.order = order;

        ByteBuffer bb = ByteBuffer.allocateDirect(coord.length * 4); // float 형 4 바이트
        bb.order(ByteOrder.nativeOrder());  // 정렬의 이유 : 네트워크 장비는 BigEndian, 인텔은 LittleEndian 으로 정렬이 되는데 이것을 자바에서 처리할 수 있도록 하는 메소드이다.

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(coord);
        vertexBuffer.position(0);

        bb = ByteBuffer.allocateDirect(order.length * 2); // short 형 2 바이트
        bb.order(ByteOrder.nativeOrder());  // 정렬의 이유 : 네트워크 장비는 BigEndian, 인텔은 LittleEndian 으로 정렬이 되는데 이것을 자바에서 처리할 수 있도록 하는 메소드이다.

        drawBuffer = bb.asShortBuffer();
        drawBuffer.put(order);
        drawBuffer.position(0);

        // 점위치 계산식
        // vertexShaderCode -> vertexShader
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode
        );

        // 점색상 계산식
        // fragmentShaderCode -> fragmentShader
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode
        );

        // mProgram -> vertexShader + fragmentShader
        mProgram = GLES20.glCreateProgram();
        //점위치 계산식 합치기
        GLES20.glAttachShader(mProgram, vertexShader);
        //점색상 계산식 합치기
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram); // 도형 렌더링 계산식 정보를 넣는다.
    }

    int mPositionHandle, mColorHandle, mMVPMatrixHandle;


    // 도형 그리기 --> MyGLRenderer.onDrawFrame() 에서 호출하여 그리기
    void draw(float [] mMVPMatrix){

        // 렌더링된 계산식 정보를 사용한다.
        GLES20.glUseProgram(mProgram);

        //       vPosition
        // mProgram ==> vertexShader
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(
                mPositionHandle,  // 정점 속성의 인덱스 지정
                3,          // 점속성 - 좌표계
                GLES20.GL_FLOAT,  // 점의 자료형 float
                false, // 정규화  true, 직접변환 false
                3 * 4,    // 점 속성의 stride(간격)
                vertexBuffer      // 점 정보
        );

        //       vColor
        // mProgram ==> fragmentShader
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        GLES20.glUniform4fv(mColorHandle, 1, color, 0);


        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // 그려지는 곳에 위치, 보이는 정보를 적용한다.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        //직사각형을 그린다.
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                order.length,
                GLES20.GL_UNSIGNED_SHORT,
                drawBuffer
                );

        // 닫는다
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
