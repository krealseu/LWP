package org.kreal.lwp.service.imageview

import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import org.kreal.glutil.VertexArray
import org.kreal.lwp.service.Transitions.Transition
import java.nio.IntBuffer

/**
 * Created by lthee on 2017/10/4.
 *
 *
 */
@Suppress("LeakingThis")
open class PhotoContainer(val mutable: Boolean, var width: Float = 1.0f, var height: Float = 1.0f) {

    var position = VertexArray(floatArrayOf(-width / 2f, -height / 2f, width / 2f, -height / 2f, -width / 2f, height / 2f, width / 2f, height / 2f))
        private set

    private val photoProgram = PhotoProgram()

    var imageOld = PhotoFrameImage(this)
        private set
    var imageNew = PhotoFrameImage(this)
        private set

    private fun swapImage() {
        val temp = imageNew
        imageNew = imageOld
        imageOld = temp
    }

    private var transition: Transition? = null

    open val setTransition: () -> Transition? = {
        null
    }

    fun draw(mvMatrix: FloatArray) {
        transition?.run {
            if (isRunning) {
                draw(mvMatrix)
                return
            } else return@run
        }
        photoProgram.useProgram()
        photoProgram.setUniforms(mvMatrix, imageNew.texture)
        position.setVertexAttribPointer(0, photoProgram.positionHandle, 2, 0)
        imageNew.textureCoordinates.setVertexAttribPointer(0, photoProgram.texPositionHandle, 2, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

    }

    fun serSrc(path: String) {
        if (imageOld.contentMatch(path)) {
            swapImage()
            transition = setTransition()
        } else if (imageNew.contentMatch(path))
            return
        else {
            imageOld.loadTexture(path)
            swapImage()
            transition = setTransition()
        }
    }

    fun setSize(width: Float, height: Float) {
        if (mutable) return
        mutableSize(width, height)
    }

    private fun mutableSize(width: Float, height: Float) {
        this.width = width
        this.height = height
        position = VertexArray(floatArrayOf(-width / 2f, -height / 2f, width / 2f, -height / 2f, -width / 2f, height / 2f, width / 2f, height / 2f))
        imageOld.updateTextureCoordinates()
        imageNew.updateTextureCoordinates()
    }

    fun recycle() {
        photoProgram.recycle()
        imageOld.recycle()
        imageNew.recycle()
    }

    class PhotoFrameImage(private val photoContainer: PhotoContainer) {
        var data: String? = null
        private var textureWidth: Float = 1.0f
        private var textureHeight: Float = 1.0f
        //        var imageRadio: Float = 1.0f
        lateinit var textureCoordinates: VertexArray

        var texture: Int = 0

        fun contentMatch(src2: String): Boolean {
            return data?.contentEquals(src2) ?: false
        }

        fun loadTexture(path: String) {
            if (contentMatch(path))
                return
            var options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)
            val tempTextureWidth = options.outWidth
            val tempTextureHeight = options.outHeight
            options.inJustDecodeBounds = false
            options.inScaled = false
            val bitmap = BitmapFactory.decodeFile(path, options) ?: return
            textureHeight = tempTextureHeight.toFloat()
            textureWidth = tempTextureWidth.toFloat()
//            imageRadio = tempTextureHeight.toFloat() / tempTextureWidth.toFloat()
            if (photoContainer.mutable)
                photoContainer.setSize(textureWidth, textureHeight)
            else
                updateTextureCoordinates()
            data = path
            if (!GLES20.glIsTexture(texture)) {
                val textures = IntArray(1)
                GLES20.glGenTextures(1, textures, 0)
                if (textures[0] == 0)
                    throw RuntimeException("Error Loading Textue")
                this.texture = textures[0]
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
            //纹理过滤
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

            bitmap.recycle()
        }

        fun recycle() {
            var intbuffer = IntBuffer.allocate(1)
            intbuffer.put(texture)
            GLES20.glDeleteTextures(1, intbuffer)
        }


        fun updateTextureCoordinates() {
            val temp = photoContainer.height * textureWidth / photoContainer.width / (textureHeight)
            textureCoordinates = when (temp > 1) {
                true -> VertexArray(floatArrayOf(0.5f - 1 / temp / 2, 1f, 0.5f + 1 / temp / 2, 1f, 0.5f - 1 / temp / 2, 0f, 0.5f + 1 / temp / 2, 0f))
                false -> VertexArray(floatArrayOf(0f, 0.5f + temp / 2, 1f, 0.5f + temp / 2, 0f, 0.5f - temp / 2, 1f, 0.5f - temp / 2))
            }
        }

    }
}