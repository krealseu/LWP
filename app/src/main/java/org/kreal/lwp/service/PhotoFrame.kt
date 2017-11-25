package org.kreal.lwp.service

import org.kreal.lwp.service.Transitions.ApertureTransition
import org.kreal.lwp.service.Transitions.CloseTransition
import org.kreal.lwp.service.Transitions.MixTransition
import org.kreal.lwp.service.Transitions.Transition
import org.kreal.lwp.service.imageview.PhotoContainer
import java.util.*

/**
 * Created by lthee on 2017/10/7.
 */

class PhotoFrame : PhotoContainer(false) {

    private var transtions: Array<Transition> = arrayOf(MixTransition(this), ApertureTransition(this), CloseTransition(this))

    private var transition: Transition? = null

    val setTransition: () -> Transition? = {
        transtions[Random().nextInt(transtions.size)].reset()
    }

    var imageOld = PhotoFrameImage()
        private set
    var imageNew = PhotoFrameImage()
        private set

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
        if (imageOld.contentMatch(path)) {
            swapImage()
            transition = setTransition()
            photoFrameImage = imageNew
        } else if (imageNew.contentMatch(path))
            return
        else {
            imageOld.loadTexture(path)
            transition = setTransition()
            swapImage()
            photoFrameImage = imageNew
        }
    }

}
