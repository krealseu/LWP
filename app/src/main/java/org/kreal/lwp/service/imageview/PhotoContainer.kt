package org.kreal.lwp.service.imageview

import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.opengl.GLES20
import android.opengl.GLUtils
import org.kreal.glutil.VertexArray
import java.nio.IntBuffer

/**
 * Created by lthee on 2017/10/4.
 *
 *
 */
open class PhotoContainer(private val mutable: Boolean, var width: Float = 1.0f, var height: Float = 1.0f) {

    var position = VertexArray(floatArrayOf(-width / 2f, -height / 2f, width / 2f, -height / 2f, -width / 2f, height / 2f, width / 2f, height / 2f))
        private set

    private val photoProgram = PhotoProgram()

    var photoFrameImage: PhotoFrameImage? = null
        set(value) {
            field = value?.bindToPhotoContainer(this)
        }

    var scaleType = ScaleType.CENTER_CROP

    open fun draw(mvMatrix: FloatArray) {
        photoFrameImage?.run {
            photoProgram.useProgram()
            photoProgram.setUniforms(mvMatrix, this.texture)
            position.setVertexAttribPointer(0, photoProgram.positionHandle, 2, 0)
            this.textureCoordinates.setVertexAttribPointer(0, photoProgram.texPositionHandle, 2, 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        }
    }

    open fun setSize(width: Float, height: Float) {
        this.width = width
        this.height = height
        position = VertexArray(floatArrayOf(-width / 2f, -height / 2f, width / 2f, -height / 2f, -width / 2f, height / 2f, width / 2f, height / 2f))
    }

    open fun recycle() {
        photoProgram.recycle()
        photoFrameImage?.recycle()
    }

    class PhotoFrameImage {
        private var state = false
        var data: String? = null
        private var textureWidth: Float = 1.0f
        private var textureHeight: Float = 1.0f
        //        var imageRadio: Float = 1.0f
        lateinit var textureCoordinates: VertexArray

        var texture: Int = 0

        fun contentMatch(src2: String): Boolean {
            if (!state) return false
            return data?.contentEquals(src2) ?: false
        }

        fun loadTexture(path: String) {
            state = false
            if (contentMatch(path))
                return
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)
            val tempTextureWidth = options.outWidth
            val tempTextureHeight = options.outHeight
            options.inJustDecodeBounds = false
            options.inScaled = false
            val bitmap = BitmapFactory.decodeFile(path, options) ?: return

            if (!GLES20.glIsTexture(texture)) {
                val textures = IntArray(1)
                GLES20.glGenTextures(1, textures, 0)
                if (textures[0] == 0)
                    throw RuntimeException("Error Loading Textue")
                this.texture = textures[0]
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
            //纹理过滤
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_DEPTH_CLEAR_VALUE.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            bitmap.recycle()

            textureHeight = tempTextureHeight.toFloat()
            textureWidth = tempTextureWidth.toFloat()
            data = path
            state = true
        }

        fun recycle() {
            val intBuffer = IntBuffer.allocate(1)
            intBuffer.put(texture)
            GLES20.glDeleteTextures(1, intBuffer)
        }


//        fun updateTextureCoordinates() {
//            val temp = photoContainer.height * textureWidth / photoContainer.width / (textureHeight)
//            textureCoordinates = when (temp > 1) {
//                true -> VertexArray(floatArrayOf(0.5f - 1 / temp / 2, 1f, 0.5f + 1 / temp / 2, 1f, 0.5f - 1 / temp / 2, 0f, 0.5f + 1 / temp / 2, 0f))
//                false -> VertexArray(floatArrayOf(0f, 0.5f + temp / 2, 1f, 0.5f + temp / 2, 0f, 0.5f - temp / 2, 1f, 0.5f - temp / 2))
//            }
//        }

        fun bindToPhotoContainer(photoContainer: PhotoContainer): PhotoFrameImage {
            if (photoContainer.mutable) {
                photoContainer.height = textureHeight
                photoContainer.width = textureWidth
            }
            val ph = photoContainer.height
            val pw = photoContainer.width
            val th = textureHeight
            val tw = textureWidth
            val points = floatArrayOf(
                    -pw / 2, ph / 2,
                    pw / 2, ph / 2,
                    -pw / 2, -ph / 2,
                    pw / 2, -ph / 2)
            val matrix = Matrix()
            val temp = ph * tw / pw / th
            when (temp > 1) {
                true -> matrix.setScale(th / ph, th / ph)
                false -> matrix.setScale(tw / pw, tw / pw)
            }

            matrix.mapPoints(points, 0, points, 0, 4)
            for (i in 0..7) {
                if (i % 2 == 0)
                    points[i] /= tw
                else points[i] /= th
                points[i] += 0.5f
            }
            textureCoordinates = VertexArray(points)
            return this
        }
    }

    enum class ScaleType {
        CENTER,
        CENTER_CROP,
        CENTER_INSIDE,
        FIT_CENTER,
        FIT_END,
        FIT_START,
        FIT_XY,
        MATRIX
    }
}