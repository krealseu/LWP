package org.kreal.lwp.service;

import android.opengl.Matrix;

/**
 * Created by lthee on 2017/6/29.
 */

public class MatrixState {
    private static float[] mProjMatrix = new float[16];//4x4矩阵 投影用
    private static float[] mVMatrix = new float[16];//摄像机位置朝向9参数矩阵
    private static float[] currMatrix;//当前变换矩阵

    static float[][] mStack = new float[10][16];
    static int stackTop = -1;

    public static void setInitStack()//获取不变换初始矩阵
    {
        currMatrix = new float[16];
        Matrix.setRotateM(currMatrix, 0, 0, 1, 0, 0);
    }

    public static void pushMatrix()//保护变换矩阵
    {
        stackTop++;
        for (int i = 0; i < 16; i++) {
            mStack[stackTop][i] = currMatrix[i];
        }
    }

    public static void popMatrix()//恢复变换矩阵
    {
        for (int i = 0; i < 16; i++) {
            currMatrix[i] = mStack[stackTop][i];
        }
        stackTop--;
    }

    public static void translate(float x, float y, float z)//设置沿xyz轴移动
    {
        Matrix.translateM(currMatrix, 0, x, y, z);
    }

    public static void rotate(float angle, float x, float y, float z)//设置绕xyz轴移动   DEG
    {
        Matrix.rotateM(currMatrix, 0, angle, x, y, z);
    }

    public static void scale(float x, float y, float z) {
        Matrix.scaleM(currMatrix, 0, x, y, z);
    }

    //插入自带矩阵
    public static void matrix(float[] self) {
        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, currMatrix, 0, self, 0);
        currMatrix = result;
    }

    public static void setProjMatrix(float[] projMatrix) {
        for (int i = 0; i < 16; i++) {
            mProjMatrix[i] = projMatrix[i];
        }
    }

    public static void setCamera(float[] camera) {
        for (int i = 0; i < 16; i++) {
            mVMatrix[i] = camera[i];
        }
    }
    //获取具体物体的总变换矩阵
    static float[] mMVPMatrix=new float[16];
    public static float[] getFinalMatrix()
    {
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, currMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }

    //获取具体物体的变换矩阵
    public static float[] getMMatrix()
    {
        return currMatrix;
    }

    //获取投影矩阵
    public static float[] getProjMatrix()
    {
        return mProjMatrix;
    }

    //获取摄像机朝向的矩阵
    public static float[] getCaMatrix()
    {
        return mVMatrix;
    }
}
