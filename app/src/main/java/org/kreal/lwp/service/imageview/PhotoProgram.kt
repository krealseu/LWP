package org.kreal.lwp.service.imageview

import android.opengl.GLES20

import org.kreal.glutil.ShaderProgram

/**
 * Created by lthee on 2017/6/5.
 * OpenGL ES 的着色器程序封装
 */

open class PhotoProgram : ShaderProgram(vertexShader, fragmentShader) {

    val positionHandle: Int
    val texPositionHandle: Int
    private val mTextureHandle: Int
    private val mViewMatrixHandle: Int

    init {
        positionHandle = GLES20.glGetAttribLocation(programID, A_POSITION)
        texPositionHandle = GLES20.glGetAttribLocation(programID, A_TEXTURE_COORDINATES)
        mTextureHandle = GLES20.glGetUniformLocation(programID, S_TEXTURE)
        mViewMatrixHandle = GLES20.glGetUniformLocation(programID, U_MVPMATRIX)
    }

    fun setUniforms(matrix: FloatArray, textureID: Int) {
        GLES20.glUniformMatrix4fv(mViewMatrixHandle, 1, false, matrix, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID)
        GLES20.glUniform1i(mTextureHandle, 0)
    }

    companion object {
        private const val vertexShader = "uniform mat4 u_MVPMatrix ;" +
                "attribute vec2 a_Position;" +
                "attribute vec2 a_TextureCoordinates;" +
                "varying vec2 v_TextureCoordinates;" +
                "void main() {" +
                "v_TextureCoordinates=a_TextureCoordinates;" +
                "gl_Position = u_MVPMatrix * vec4(a_Position,0,1);" +
                "}"
        private const val fragmentShader = "precision mediump float;" +
                "varying vec2 v_TextureCoordinates;" +
                "uniform sampler2D s_Texture;" +
                "void main() {" +
                " vec4 texture = texture2D(s_Texture,v_TextureCoordinates);" +
                "  gl_FragColor = texture;" +
                "}"
        private const val U_MVPMATRIX = "u_MVPMatrix"
        private const val A_POSITION = "a_Position"
        private const val A_TEXTURE_COORDINATES = "a_TextureCoordinates"
        private const val S_TEXTURE = "s_Texture"
    }
}
