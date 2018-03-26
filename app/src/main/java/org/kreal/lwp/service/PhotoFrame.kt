package org.kreal.lwp.service


import org.kreal.lwp.service.imageview.PhotoContainer
import org.kreal.lwp.service.transitions.*
import java.util.*

/**
 * Created by lthee on 2017/10/7.
 * 将容器封装为相框
 */

class PhotoFrame : PhotoContainer(false) {

    private var transtions: Array<Transition> = arrayOf(MixTransition(this), ApertureTransition(this), CloseTransition(this), BlindsTransition(this))

    private var transition: Transition? = null

    private val setTransition: () -> Transition? = {
        transtions[Random().nextInt(transtions.size)].reset()
    }

    var imageOld = PhotoFrameImage()
        private set
    var imageNew = PhotoFrameImage()
        private set

    fun setAnimationTime(duration: Float) {
        transtions.forEach { it.setDuration(duration) }
    }

    override fun draw(mvMatrix: FloatArray) {
        transition?.run {
            if (isRunning) {
                draw(mvMatrix)
                return
            } else return@run
        }
        super.draw(mvMatrix)
    }

    override fun setSize(width: Float, height: Float) {
        super.setSize(width, height)
        imageNew.bindToPhotoContainer(this)
        imageOld.bindToPhotoContainer(this)
    }

    private fun swapImage() {
        val temp = imageNew
        imageNew = imageOld
        imageOld = temp
    }

    fun setSrc(path: String) {
        when {
            imageOld.contentMatch(path) -> {
                swapImage()
                transition = setTransition()
                photoFrameImage = imageNew
            }
            imageNew.contentMatch(path) -> Unit
            else -> {
                imageOld.loadTexture(path)
                transition = setTransition()
                swapImage()
                photoFrameImage = imageNew
            }
        }
    }

    override fun recycle() {
        super.recycle()
        transtions.forEach { it.recycle() }
    }
}
