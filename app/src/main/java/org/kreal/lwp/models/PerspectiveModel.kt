package org.kreal.lwp.models

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock

/**
 * Created by lthee on 2017/5/26.
 */

class PerspectiveModel(context: Context) : SensorEventListener {
    private val mSensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val mSensor: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var mEnabled = false
    private val maxAngle = 1f
    private val T = 40.0f

    private var xAngle = 0f
    private var yAngle = 0f
    private var xVelocity = 0f
    private var yVelocity = 0f
    private var lastSamplingTime: Long = 0

//    var dpi: Float = 0.toFloat()     // pixels per inch
//    private val mVelocity = 0.08f //inch per rad
//    val offset: Float
//    val persPectiveScale: Float

    init {
//        val dm = context.resources.displayMetrics
//        dpi = (dm.xdpi + dm.ydpi) / 2
//        offset = dpi * mVelocity * MaxValue * 2f

//        val temph = 1.0f * offset / dm.heightPixels
//        val tempw = 1.0f * offset / dm.widthPixels
//        persPectiveScale = 1.0f + if (temph > tempw) temph else tempw
    }

    fun enable() {
//        if (mSensor == null) {
//            Log.w(TAG, "Cannot detect sensors. Not enabled")
//            return
//        }
        if (!mEnabled) {
            //            xAngle = 0;
            //            yAngle = 0;
            xVelocity = 0f
            yVelocity = 0f
            lastSamplingTime = SystemClock.elapsedRealtimeNanos()
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI)
            mEnabled = true
        }
    }

    fun disable() {
//        if (mSensor == null) {
//            Log.w(TAG, "Cannot detect sensors. Invalid disable")
//            return
//        }
        if (mEnabled) {
            mSensorManager.unregisterListener(this)
            mEnabled = false
        }
    }


    override fun onSensorChanged(event: SensorEvent) {
        val time = SystemClock.elapsedRealtimeNanos()
        val dT = (time - lastSamplingTime) * NS2S
        lastSamplingTime = time
        //        LogHelp.LogI(dT);
        if (dT > 0.1)
            return
        xAngle += xVelocity * dT
        yAngle += +yVelocity * dT
        xAngle += if (xAngle > 0) -dT / T * maxAngle else dT / T * maxAngle
        yAngle += if (yAngle > 0) -dT / T * maxAngle else dT / T * maxAngle

        xAngle = limitThreshold(xAngle, maxAngle)
        yAngle = limitThreshold(yAngle, maxAngle)

        xVelocity = limitThreshold(event.values[0], 5f)//filter(event.values[0]);//
        yVelocity = limitThreshold(event.values[1], 5f)//filter(event.values[1]);//
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    fun getValue(orientation: Int = 0): FloatArray {
        val time = SystemClock.elapsedRealtimeNanos()
        val dT = (time - lastSamplingTime) * NS2S
        var tempX = xAngle + xVelocity * dT
        var tempY = yAngle + yVelocity * dT
        tempX += if (tempX > 0) -dT / T * maxAngle else dT / T * maxAngle
        tempY += if (tempY > 0) -dT / T * maxAngle else dT / T * maxAngle

        tempX = limitThreshold(tempX, maxAngle) //* mVelocity * dpi
        tempY = limitThreshold(tempY, maxAngle)

        return when (orientation) {
            0 -> floatArrayOf(tempX, tempY)
            1 -> floatArrayOf(-tempY, tempX)
            2 -> floatArrayOf(-tempY, -tempX)
            3 -> floatArrayOf(tempY, -tempX)
            else -> floatArrayOf(0f, 0f)
        }
    }

    private fun limitThreshold(input: Float, Threshold: Float): Float {
        return when {
            input > Threshold -> Threshold
            input < -Threshold -> -Threshold
            else -> input
        }
    }

    private fun filter(input: Float): Float {
        val max = 3f
        return if (input >= 0) (1 - 1 / (input + 1)) * max else -(1 - 1 / (1 - input)) * max
    }

    companion object {
        private const val NS2S = 1.0f / 1000000000.0f
    }

}
