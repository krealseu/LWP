package org.kreal.lwp.service

import org.kreal.lwp.service.Transitions.ApertureTransition
import org.kreal.lwp.service.Transitions.CloseTransition
import org.kreal.lwp.service.Transitions.MixTransition
import org.kreal.lwp.service.Transitions.Transition
import org.kreal.lwp.service.imageview.PhotoContainer
import java.util.*

/**
 * Created by lthee on 2017/10/6.
 */
class PhotoFrame : PhotoContainer(false) {
    private var transtions: Array<Transition> = arrayOf(MixTransition(this), ApertureTransition(this), CloseTransition(this))

    override val setTransition: () -> Transition? = {
        transtions[Random().nextInt(transtions.size)].reset()

    }

}