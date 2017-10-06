package org.kreal.lwp.service.Transitions

import android.os.SystemClock
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

/**
 * Created by lthee on 2017/10/4.
 */
abstract class Transition(val duration: Float = 1.0f) {
    var isRunning = false
        private set
    open protected var mInterpolator: Interpolator = LinearInterpolator()
    private var mStartTime: Long = -1
    fun reset():Transition {
        isRunning = true
        mStartTime = -1
        return this
    }

    fun draw(matrix: FloatArray) {
        if (mStartTime == -1L) {
            mStartTime = SystemClock.uptimeMillis()
        }
        val delta = getDetla()
        applyTransition(matrix, delta)
        isRunning = delta < 1f
    }

    abstract fun applyTransition(matrix: FloatArray, delta: Float)

    abstract fun recycle()

    private fun getDetla(): Float {
        val delta = Math.min((SystemClock.uptimeMillis() - mStartTime).toFloat() / 1000f, duration) / duration
        return mInterpolator.getInterpolation(delta)
    }

}