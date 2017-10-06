package org.kreal.lwp.service.Transitions

import android.opengl.GLES20
import org.kreal.glutil.ShaderProgram
import org.kreal.glutil.VertexArray
import org.kreal.lwp.service.imageview.PhotoContainer

/**
 * Created by lthee on 2017/10/5.
 */
class CloseTransition(private val target: PhotoContainer, duration: Float = 0.6f) : Transition(duration) {
    private val program = object : ShaderProgram(vertexShader, fragmentShader) {
        val positionHandle = GLES20.glGetAttribLocation(programID, A_POSOTIION)
        val fragcoordHandle = GLES20.glGetAttribLocation(programID, A_FLSGCOORD)
        val texPositionHandle1 = GLES20.glGetAttribLocation(programID, A_TEXTURE_COORDINATES1)
        val textureHandle1 = GLES20.glGetUniformLocation(programID, S_TEXTURE1)
        val texPositionHandle2 = GLES20.glGetAttribLocation(programID, A_TEXTURE_COORDINATES2)
        val textureHandle2 = GLES20.glGetUniformLocation(programID, S_TEXTURE2)
        val viewMatrixHandle = GLES20.glGetUniformLocation(programID, U_MVPMATRIX)
        val radius = GLES20.glGetUniformLocation(programID, U_RADIUS)

        fun setUniforms(matrix: FloatArray, textureID1: Int, textureID2: Int, radius: Float) {
            GLES20.glUniformMatrix4fv(viewMatrixHandle, 1, false, matrix, 0)
            GLES20.glUniform1f(this.radius, radius)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID1)
            GLES20.glUniform1i(textureHandle1, 0)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID2)
            GLES20.glUniform1i(textureHandle2, 1)
        }
    }
    private val texcoord = VertexArray(floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f))

    override fun applyTransition(matrix: FloatArray, delta: Float) {
        program.useProgram()
        val texture1 = target.imageOld.texture
        val texture2 = target.imageNew.texture
        program.setUniforms(matrix, texture1, texture2, delta)
        target.position.setVertexAttribPointer(0, program.positionHandle, 2, 0)
        target.imageOld.textureCoordinates.setVertexAttribPointer(0, program.texPositionHandle1, 2, 0)
        target.imageNew.textureCoordinates.setVertexAttribPointer(0, program.texPositionHandle2, 2, 0)
        texcoord.setVertexAttribPointer(0, program.fragcoordHandle, 2, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    override fun recycle() {
        program.recycle()
    }

    companion object {
        private val vertexShader = "uniform mat4 u_MVPMatrix ;" +
                "attribute vec2 a_Postion;" +
                "attribute vec2 a_mFlagCoord;" +
                "varying vec2 v_mFlagCoord;" +
                "attribute vec2 a_TextureCoordinates1;" +
                "varying vec2 v_TextureCoord1;" +
                "attribute vec2 a_TextureCoordinates2;" +
                "varying vec2 v_TextureCoord2;" +
                "void main() {" +
                "v_mFlagCoord = a_mFlagCoord;" +
                "v_TextureCoord1=a_TextureCoordinates1;" +
                "v_TextureCoord2=a_TextureCoordinates2;" +
                "gl_Position = u_MVPMatrix * vec4(a_Postion,0,1);" +
                "}"
        private val fragmentShader = "precision mediump float;" +
                "varying vec2 v_mFlagCoord;" +
                "varying vec2 v_TextureCoord1;" +
                "uniform sampler2D s_Texture1;" +
                "varying vec2 v_TextureCoord2;" +
                "uniform sampler2D s_Texture2;" +
                "uniform float radius;" +
                "void main() {" +
                "  vec4 tex1 = texture2D(s_Texture1,v_TextureCoord1);" +
                "  vec4 tex2 = texture2D(s_Texture2,v_TextureCoord2);" +
                "  vec2 uv = v_mFlagCoord;" +
                "  bool inCircle = uv.x < radius / 2.0 || uv.x > (1.0 - radius / 2.0);" +
                " if (inCircle) {" +
                "    gl_FragColor = tex2;" +
                "    } else {" +
                "        gl_FragColor = tex1;" +
                "    }" +
                "}"
        private val fragmentShader1 = "precision mediump float;" +
                "varying vec2 v_mFlagCoord;" +
                "varying vec2 v_TextureCoord1;" +
                "uniform sampler2D s_Texture1;" +
                "varying vec2 v_TextureCoord2;" +
                "uniform sampler2D s_Texture2;" +
                "uniform float radius;" +
                "void main() {" +
                "  vec4 tex1 = texture2D(s_Texture1,v_TextureCoord1);" +
                "  vec4 tex2 = texture2D(s_Texture2,v_TextureCoord2);" +
                "  vec2 uv = v_mFlagCoord;" +
                "  bool inCircle = uv.x < radius / 2.0 || uv.x > (1.0 - radius / 2.0);" +
                " if (inCircle) {" +
                "    gl_FragColor = tex2;" +
                "    } else {" +
                "        gl_FragColor = tex1;" +
                "    }" +
                "}"
        private val U_MVPMATRIX = "u_MVPMatrix"
        private val A_POSOTIION = "a_Postion"
        private val A_FLSGCOORD = "a_mFlagCoord"
        private val A_TEXTURE_COORDINATES1 = "a_TextureCoordinates1"
        private val A_TEXTURE_COORDINATES2 = "a_TextureCoordinates2"
        private val U_RADIUS = "radius"
        private val S_TEXTURE1 = "s_Texture1"
        private val S_TEXTURE2 = "s_Texture2"
    }
}