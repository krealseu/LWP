package org.kreal.lwp.service.Transitions

import android.opengl.GLES20
import org.kreal.glutil.ShaderProgram
import org.kreal.lwp.service.PhotoFrame

/**
 * Created by lthee on 2017/10/4.
 */
class MixTransition(val targer: PhotoFrame) : Transition() {

    private val program = object : ShaderProgram(vertexShader, fragmentShader) {
        val positionHandle = GLES20.glGetAttribLocation(programID, A_POSOTIION)
        val texPositionHandle1 = GLES20.glGetAttribLocation(programID, A_TEXTURE_COORDINATES1)
        val textureHandle1 = GLES20.glGetUniformLocation(programID, S_TEXTURE1)
        val texPositionHandle2 = GLES20.glGetAttribLocation(programID, A_TEXTURE_COORDINATES2)
        val textureHandle2 = GLES20.glGetUniformLocation(programID, S_TEXTURE2)
        val viewMatrixHandle = GLES20.glGetUniformLocation(programID, U_MVPMATRIX)
        val delta = GLES20.glGetUniformLocation(programID, U_DELTER)

        fun setUniforms(matrix: FloatArray, textureID1: Int, textureID2: Int, delta: Float) {
            GLES20.glUniformMatrix4fv(viewMatrixHandle, 1, false, matrix, 0)
            GLES20.glUniform1f(this.delta, delta)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID1)
            GLES20.glUniform1i(textureHandle1, 0)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID2)
            GLES20.glUniform1i(textureHandle2, 1)
        }
    }

    override fun applyTransition(matrix: FloatArray, delta: Float) {
        program.useProgram()
        val texture1 = targer.imageOld.texture
        val texture2 = targer.imageNew.texture
        program.setUniforms(matrix, texture1, texture2, delta)
        targer.position.setVertexAttribPointer(0, program.positionHandle, 2, 0)
        targer.imageOld.textureCoordinates.setVertexAttribPointer(0, program.texPositionHandle1, 2, 0)
        targer.imageNew.textureCoordinates.setVertexAttribPointer(0, program.texPositionHandle2, 2, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    override fun recycle() {
        program.recycle()
    }

    companion object {
        private val vertexShader = "uniform mat4 u_MVPMatrix ;" +
                "attribute vec2 a_Postion;" +
                "attribute vec2 a_TextureCoordinates1;" +
                "varying vec2 v_TextureCoord1;" +
                "attribute vec2 a_TextureCoordinates2;" +
                "varying vec2 v_TextureCoord2;" +
                "void main() {" +
                "v_TextureCoord1=a_TextureCoordinates1;" +
                "v_TextureCoord2=a_TextureCoordinates2;" +
                "gl_Position = u_MVPMatrix * vec4(a_Postion,0,1);" +
                "}"
        private val fragmentShader = "precision mediump float;" +
                "varying vec2 v_TextureCoord1;" +
                "uniform sampler2D s_Texture1;" +
                "varying vec2 v_TextureCoord2;" +
                "uniform sampler2D s_Texture2;" +
                "uniform float u_delta;" +
                "void main() {" +
                " vec4 texture1 = texture2D(s_Texture1,v_TextureCoord1);" +
                " vec4 texture2 = texture2D(s_Texture2,v_TextureCoord2);" +
                "  gl_FragColor = mix(texture1,texture2,u_delta);" +
                "}"
        private val U_MVPMATRIX = "u_MVPMatrix"
        private val A_POSOTIION = "a_Postion"
        private val A_TEXTURE_COORDINATES1 = "a_TextureCoordinates1"
        private val A_TEXTURE_COORDINATES2 = "a_TextureCoordinates2"
        private val U_DELTER = "u_delta"
        private val S_TEXTURE1 = "s_Texture1"
        private val S_TEXTURE2 = "s_Texture2"
    }

}