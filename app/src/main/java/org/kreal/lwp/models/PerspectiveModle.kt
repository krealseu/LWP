package org.kreal.lwp.models

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log

/**
 * Created by lthee on 2017/5/26.
 */

class PerspectiveModle(context: Context) : SensorEventListener {
    private val mSensor: Sensor
    private val mSensorManager: SensorManager
    private var mEnabled = false
    private val MaxAngle = 1f
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
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
//        val dm = context.resources.displayMetrics
//        dpi = (dm.xdpi + dm.ydpi) / 2
//        offset = dpi * mVelocity * MaxValue * 2f

//        val temph = 1.0f * offset / dm.heightPixels
//        val tempw = 1.0f * offset / dm.widthPixels
//        persPectiveScale = 1.0f + if (temph > tempw) temph else tempw

    }

    public fun enable() {
//        if (mSensor == null) {
//            Log.w(TAG, "Cannot detect sensors. Not enabled")
//            return
//        }
        if (mEnabled == false) {
            //            xAngle = 0;
            //            yAngle = 0;
            xVelocity = 0f
            yVelocity = 0f
            lastSamplingTime = SystemClock.elapsedRealtimeNanos()
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI)
            mEnabled = true
        }
    }

    public fun disable() {
//        if (mSensor == null) {
//            Log.w(TAG, "Cannot detect sensors. Invalid disable")
//            return
//        }
        if (mEnabled == true) {
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
        xAngle = xAngle + xVelocity * dT
        yAngle = yAngle + yVelocity * dT
        xAngle += if (xAngle > 0) -dT / T * MaxAngle else dT / T * MaxAngle
        yAngle += if (yAngle > 0) -dT / T * MaxAngle else dT / T * MaxAngle

        xAngle = limitThreshold(xAngle, MaxAngle)
        yAngle = limitThreshold(yAngle, MaxAngle)

        xVelocity = limitThreshold(event.values[0], 5f)//filter(event.values[0]);//
        yVelocity = limitThreshold(event.values[1], 5f)//filter(event.values[1]);//
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    public fun getValue(orientation: Int = 0) :FloatArray{
        var result =FloatArray(2)
        val time = SystemClock.elapsedRealtimeNanos()
        val dT = (time - lastSamplingTime) * NS2S
        var tempx = xAngle + xVelocity * dT
        var tempy = yAngle + yVelocity * dT
        tempx += if (tempx > 0) -dT / T * MaxAngle else dT / T * MaxAngle
        tempy += if (tempy > 0) -dT / T * MaxAngle else dT / T * MaxAngle

        tempx = limitThreshold(tempx, MaxAngle)
        tempy = limitThreshold(tempy, MaxAngle)

        when (orientation) {
            0 -> {
                result[0] = tempx //* mVelocity * dpi
                result[1] = tempy //* mVelocity * dpi
            }
            1 -> {
                result[1] = tempx //* mVelocity * dpi
                result[0] = -tempy //* mVelocity * dpi
            }
            2 -> {
                result[1] = -tempx// * mVelocity * dpi
                result[0] = -tempy //* mVelocity * dpi
            }
            3 -> {
                result[1] = -tempx //* mVelocity * dpi
                result[0] = tempy //* mVelocity * dpi
            }
        }
        return result
    }

    private fun limitThreshold(input: Float, Threshold: Float): Float {
        val result: Float
        when {
            input > Threshold -> result = Threshold
            input < -Threshold -> result = -Threshold
            else -> result = input
        }
        return result
    }

    private fun filter(input: Float): Float {
        val result: Float
        val max = 3f
        if (input >= 0)
            result = (1 - 1 / (input + 1)) * max
        else
            result = -(1 - 1 / (1 - input)) * max
        return result
    }

    companion object {
        private val TAG = PerspectiveModle::class.java.simpleName
        private val NS2S = 1.0f / 1000000000.0f
    }

}
