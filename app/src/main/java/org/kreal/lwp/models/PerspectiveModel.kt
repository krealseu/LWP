package org.kreal.lwp.models

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 * Created by lthee on 2017/5/26.
 * 处理透视运动
 */

class PerspectiveModel(context: Context) : SensorEventListener {
    private val tag = PerspectiveModel::class.java.simpleName
    private val mSensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val mGyroSensor: Sensor? = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val mAccelerationSensor: Sensor? = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private var mEnabled = false
    private val maxAngle = 1f
    private val T = 40.0f
    private var lastEvent: Long? = null
    private val accelerator: FloatArray = floatArrayOf(0f, 0f, 0f)
    private val palstance: FloatArray = floatArrayOf(0f, 0f, 0f)
    private val velocity = floatArrayOf(0f, 0f, 0f)
    private val angle = floatArrayOf(0f, 0f, 0f)

    private var lastSamplingTime: Long = 0


    fun enable() {
        if (mGyroSensor == null) {
            Log.w(tag, "Cannot detect sensors. Not enabled")
            return
        }
        if (!mEnabled) {
            lastEvent = null
            lastSamplingTime = System.nanoTime()
            mSensorManager.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_UI)
//            mSensorManager.registerListener(this, mAccelerationSensor, SensorManager.SENSOR_DELAY_UI)
            mEnabled = true
        }
    }

    fun disable() {
        if (mGyroSensor == null) {
            Log.w(tag, "Cannot detect sensors. Invalid disable")
            return
        }
        if (mEnabled) {
            mSensorManager.unregisterListener(this)
            lastEvent = null
            mEnabled = false
        }
    }


    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> event.values.copyTo(accelerator)
            Sensor.TYPE_GYROSCOPE -> for (i in 0..2) {
                palstance[i] = 3f * 1.5f * exp(event.values[i]) / (3f + 1.5f * (exp(event.values[i]) - 1)) - 1.5f
                //max(min(event.values[i], 3f), -3f)
                // 4f * 2f * exp(event.values[i]) / (4f + 2f * (exp(event.values[i]) - 1)) - 2f
            }
        }
        lastEvent?.also {
            val dT = (event.timestamp - it) * NS2S
            for (i in 0..2) {
                velocity[i] += accelerator[i] * dT
                velocity[i] *= (1f - dT / 10)
                angle[i] += palstance[i] * dT - sign(angle[i]) * dT / T * maxAngle
                angle[i] = limitThreshold(angle[i], maxAngle)
            }
            lastSamplingTime = System.nanoTime()
        }
        lastEvent = event.timestamp

    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    fun getValue(orientation: Int = 0): FloatArray {
        val result = angle.clone()
        lastEvent?.also {
            val time = System.nanoTime()
            val dT = (time - lastSamplingTime) * NS2S
            for (i in 0..2) {
                result[i] += palstance[i] * dT - sign(angle[i]) * dT / T * maxAngle
                result[i] = limitThreshold(result[i], maxAngle)
            }
        }
        return when (orientation) {
            0 -> floatArrayOf(result[0], result[1])
            1 -> floatArrayOf(-result[1], result[0])
            2 -> floatArrayOf(-result[1], -result[0])
            3 -> floatArrayOf(result[1], -result[0])
            else -> floatArrayOf(0f, 0f)
        }
    }

    private val limitThreshold: ((Float, Float) -> Float) = { input, threshold -> max(min(input, threshold), -threshold) }

    private fun FloatArray.copyTo(target: FloatArray) {
        for (i in indices)
            target[i] = this[i]
    }

    companion object {
        private const val NS2S = 1.0f / 1000000000.0f
    }


}

