package org.kreal.lwp.service.transitions

import android.os.SystemClock
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

/**
 * Created by lthee on 2017/10/4.
 * 过渡动画的基类
 */
abstract class Transition(private var duration: Float = 1.0f) {
    var isRunning = false
        private set
    protected open var mInterpolator: Interpolator = LinearInterpolator()
    private var mStartTime: Long = -1
    fun reset(): Transition {
        isRunning = true
        mStartTime = -1
        return this
    }

    fun draw(matrix: FloatArray) {
        if (mStartTime == -1L) {
            mStartTime = SystemClock.uptimeMillis()
        }
        val delta = getDelta()
        applyTransition(matrix, delta)
        isRunning = delta < 1f
    }

    fun setDuration(duration: Float) {
        this.duration = duration
    }

    abstract fun applyTransition(matrix: FloatArray, delta: Float)

    abstract fun recycle()

    private fun getDelta(): Float {
        val delta = Math.min((SystemClock.uptimeMillis() - mStartTime).toFloat() / 1000f, duration) / duration
        return mInterpolator.getInterpolation(delta)
    }

}