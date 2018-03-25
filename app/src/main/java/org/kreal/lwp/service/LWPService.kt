package org.kreal.lwp.service

import android.content.*
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Environment
import android.preference.PreferenceManager
import android.view.SurfaceHolder
import android.view.WindowManager
import org.kreal.lwp.models.PerspectiveModel
import org.kreal.lwp.models.WallpaperManager
import org.kreal.lwp.settings.*
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by lthee on 2017/10/3.
 * 壁纸的server，使用GLView
 */
class LWPService : GLWallpaperService() {
    var batteryLow = false
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_LOW -> batteryLow = true
                Intent.ACTION_POWER_CONNECTED -> batteryLow = false
            }
        }
    }

    override fun onCreateEngine(): Engine {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW)
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        baseContext.registerReceiver(batteryReceiver, intentFilter)
        return SwitchEngine()
    }

    override fun onDestroy() {
        super.onDestroy()
        baseContext.unregisterReceiver(batteryReceiver)
    }

    inner class SwitchEngine : GLEngine(), GLSurfaceView.Renderer, SharedPreferences.OnSharedPreferenceChangeListener {

        private val mVMatrix = FloatArray(16)

        private val mPMatrix = FloatArray(16)

        private var perspectiveModel = PerspectiveModel(baseContext)

        private lateinit var photoFrame: PhotoFrame

        private var display = (applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

        private var mPerspectiveScale = PreferenceManager.getDefaultSharedPreferences(baseContext).getFloat(PhotoFrameScale, 1.0f)

        private val fpsControl: FPSControl = FPSControl(PreferenceManager.getDefaultSharedPreferences(baseContext).getString(FPSControl, "30").toInt())

        private val wallpapers = WallpaperManager(baseContext.getFileStreamPath(WallpaperSource))

        private var refreshTime: Long = (PreferenceManager.getDefaultSharedPreferences(baseContext).getString(RefreshTime, "10").toFloat() * 60000).toLong()

        private var canPerspectiveMove = PreferenceManager.getDefaultSharedPreferences(baseContext).getBoolean(CanPerspectiveMove, true)

        private var canMove: Boolean = PreferenceManager.getDefaultSharedPreferences(baseContext).getBoolean(CanMove, false)

        private var animationTime: Long = PreferenceManager.getDefaultSharedPreferences(baseContext).getString(AnimationTime, "1000").toLong()

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            when (key) {
                RefreshTime -> refreshTime = (sharedPreferences.getString(key, "10").toFloat() * 60000).toLong()
                CanMove -> canMove = sharedPreferences.getBoolean(key, false)
                CanPerspectiveMove -> canPerspectiveMove = sharedPreferences.getBoolean(key, true)
                FPSControl -> fpsControl.setFPS(sharedPreferences.getString(key, "30").toInt())
                AnimationTime -> animationTime = sharedPreferences.getString(key, "1000").toLong()
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            PreferenceManager.getDefaultSharedPreferences(baseContext).registerOnSharedPreferenceChangeListener(this)
            setEGLContextClientVersion(2)
            setRenderer(this)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setPreserveEGLContextOnPause(true)
//            }
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            val radio = height.toFloat() / width.toFloat()
            photoFrame.setSize(2f * mPerspectiveScale, 2f * radio * mPerspectiveScale)
            Matrix.orthoM(mPMatrix, 0, -1.0f, 1.0f, -radio, radio, 1f, 3f)
            MatrixState.setProjMatrix(mPMatrix)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            Matrix.setLookAtM(mVMatrix, 0, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 1f, 0f)
            photoFrame = PhotoFrame()
            photoFrame.setSrc(wallpapers.getRandomWallpaper())
            MatrixState.setCamera(mVMatrix)
        }

        override fun onDrawFrame(gl: GL10?) {
            MatrixState.setInitStack()
            val screenRotation = display.rotation
            val perspectivePos: FloatArray = if (canPerspectiveMove) perspectiveModel.getValue(screenRotation) else floatArrayOf(0f, 0f)
            MatrixState.translate(perspectivePos[1] * (mPerspectiveScale - 1) / 2, -perspectivePos[0] * (mPerspectiveScale - 1) / 2, 0f)
            photoFrame.draw(MatrixState.getFinalMatrix())
            fpsControl.blockWait()
            requestRender()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                if (canPerspectiveMove && !batteryLow) {
                    perspectiveModel.enable()
                }
                if (isNeedRefresh()) {
                    changeWallpaper()
                }
                requestRender()
            } else {
                perspectiveModel.disable()
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            PreferenceManager.getDefaultSharedPreferences(baseContext).unregisterOnSharedPreferenceChangeListener(this)
        }

        private fun changeWallpaper() {
            queueEvent(Runnable {
                val name = wallpapers.getRandomWallpaper()
                photoFrame.setSrc(name)
                requestRender()
            })
        }

        private var lastRefreshTime: Long = System.currentTimeMillis()

        private fun isNeedRefresh(): Boolean {
            val time = System.currentTimeMillis()
            return if (Math.abs(time - lastRefreshTime) > refreshTime) {
                lastRefreshTime = time
                true
            } else false
        }

    }

    class FPSControl(fps: Int) {

        private val nanosPERMilli = 1000000

        private val nanosPERSecond = 1000000000

        private var frameDuration: Long = (nanosPERSecond / fps).toLong()

        fun setFPS(fps: Int) {
            frameDuration = (nanosPERSecond / fps).toLong()
        }

        private var lastFpsInfo: FpsInfo = FpsInfo(System.nanoTime(), 0, 0)

        fun asyncWait(request: () -> Unit) {
            val showTime = System.nanoTime()
            Thread {
                val workDuration = showTime - lastFpsInfo.leaveTime

                val nextWorkDuration = (workDuration + lastFpsInfo.workDuration) shr 1

                val tmp = frameDuration - nextWorkDuration + showTime
                val timeToSleepNanos = tmp - System.nanoTime()
                if (timeToSleepNanos > 0)
                    Thread.sleep(timeToSleepNanos / nanosPERMilli, (timeToSleepNanos % nanosPERMilli).toInt())
                lastFpsInfo = FpsInfo(showTime, workDuration, System.nanoTime())
                request()
            }.start()
        }

        fun blockWait() {
            val showTime = System.nanoTime()
            val workDuration = showTime - lastFpsInfo.leaveTime

            val nextWorkDuration = (workDuration + lastFpsInfo.workDuration) shr 1

            val tmp = frameDuration - nextWorkDuration + showTime
            val timeToSleepNanos = tmp - System.nanoTime()
            if (timeToSleepNanos > 0)
                Thread.sleep(timeToSleepNanos / nanosPERMilli, (timeToSleepNanos % nanosPERMilli).toInt())
            lastFpsInfo = FpsInfo(showTime, workDuration, System.nanoTime())
        }

        data class FpsInfo(var showTime: Long, var workDuration: Long, val leaveTime: Long)
    }

}